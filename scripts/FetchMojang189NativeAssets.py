#!/usr/bin/env python3
"""Verify Mojang 1.8.9 Linux natives and asset index, optionally stage all assets.

--full-assets downloads the complete official 1.8 asset index (734 logical entries)
with bounded concurrent streaming and per-object SHA-1 verification. Neither
mode starts Minecraft or claims a graphical/gameplay test. Official downloaded
binaries stay transient and must not be uploaded as CI artifacts.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
from io import BytesIO
import hashlib
import json
from pathlib import Path
import re
import shutil
import sys
from urllib.parse import urlsplit
from urllib.request import Request, urlopen
from zipfile import ZipFile, BadZipFile

from FetchMojang189Libraries import (
    CLIENT_SHA1, METADATA_SHA1, METADATA_URL, MAX_METADATA,
    allowed_on_linux, fetch_bytes, DIGEST, SAFE_PATH
)

MAX_NATIVE_ARCHIVE = 32 * 1024 * 1024
MAX_NATIVE_EXPANDED = 96 * 1024 * 1024
MAX_INDEX_SIZE = 8 * 1024 * 1024
MAX_SAMPLE_OBJECT = 12 * 1024 * 1024
MAX_SAMPLED_BYTES = 36 * 1024 * 1024
MAX_ASSET_OBJECTS = 12000
EXPECTED_INDEX_OBJECTS = 734
EXPECTED_INDEX_SHA1 = "f6ad102bcaa53b1a58358f16e376d548d44933ec"
MAX_COMPLETE_OBJECT = 64 * 1024 * 1024
MAX_COMPLETE_TOTAL = 1024 * 1024 * 1024
COMPLETE_DOWNLOAD_WORKERS = 8


def verify_download(url, sha1, expected_size, limit, hosts):
    if not DIGEST.fullmatch(sha1):
        raise ValueError("official resource is missing a valid SHA-1")
    if not isinstance(expected_size, int) or not (0 < expected_size <= limit):
        raise ValueError("unbounded or invalid Mojang resource size")
    parsed = urlsplit(url)
    if (parsed.scheme != "https" or parsed.hostname not in hosts
            or parsed.port or parsed.username or parsed.password
            or parsed.query or parsed.fragment):
        raise ValueError("untrusted Mojang asset-index/object download URL")
    request = Request(url, headers={"User-Agent": "customMCclient-189-resources/1"})
    digest = hashlib.sha1()
    result = bytearray()
    with urlopen(request, timeout=90) as response:
        if response.geturl() != url:
            raise ValueError("Mojang resource redirected unexpectedly")
        while True:
            part = response.read(1024 * 1024)
            if not part:
                break
            if len(result) + len(part) > limit:
                raise ValueError("Mojang resource exceeds size cap")
            result.extend(part)
            digest.update(part)
    if len(result) != expected_size or digest.hexdigest() != sha1:
        raise ValueError("Mojang resource size/SHA-1 mismatch")
    return bytes(result)


def safe_member(name):
    return bool(
        name and SAFE_PATH.fullmatch(name) and not name.startswith("/")
        and len(name) <= 400
        and all(piece not in ("", ".", "..") for piece in name.rstrip("/").split("/"))
    )


def stage_natives(metadata, root):
    libraries = metadata.get("libraries")
    if not isinstance(libraries, list):
        raise ValueError("official metadata has no library list")
    native_root = root / "natives"
    native_root.mkdir()
    extracted = set()
    archive_count = 0
    total_written = 0
    for library in libraries:
        if not allowed_on_linux(library):
            continue
        mapping = library.get("natives")
        if not isinstance(mapping, dict) or "linux" not in mapping:
            continue
        classifier = mapping["linux"].replace("${arch}", "64")
        info = library.get("downloads", {}).get("classifiers", {}).get(classifier)
        if not isinstance(info, dict):
            raise ValueError("official Linux native classifier is missing: " + classifier)
        relative = info.get("path")
        if not isinstance(relative, str) or not safe_member(relative):
            raise ValueError("unsafe native archive path")
        if info.get("url") != "https://libraries.minecraft.net/" + relative:
            raise ValueError("native classifier URL differs from Mojang metadata host")
        checksum = info.get("sha1")
        size = info.get("size")
        archive = fetch_bytes(info["url"], checksum, size=size, cap=MAX_NATIVE_ARCHIVE)
        archive_count += 1
        exclusions = library.get("extract", {}).get("exclude", ["META-INF/"])
        if not isinstance(exclusions, list) or not all(
                isinstance(ex, str) and safe_member(ex) for ex in exclusions):
            raise ValueError("unsafe native extraction exclusions")
        with ZipFile(BytesIO(archive)) as zip_file:
            for member in zip_file.infolist():
                name = member.filename
                if any(name.startswith(ex) for ex in exclusions):
                    continue
                if not safe_member(name):
                    raise ValueError("unsafe archive member: " + name)
                mode = (member.external_attr >> 16) & 0o170000
                if mode == 0o120000:
                    raise ValueError("native archive contains symlink")
                if member.is_dir():
                    continue
                if name in extracted:
                    raise ValueError("collision across official native archives: " + name)
                if member.file_size > MAX_NATIVE_EXPANDED:
                    raise ValueError("unbounded native archive member")
                if total_written + member.file_size > MAX_NATIVE_EXPANDED:
                    raise ValueError("native uncompressed budget exceeded")
                destination = native_root.joinpath(*name.split("/"))
                destination.parent.mkdir(parents=True, exist_ok=True)
                count = 0
                with zip_file.open(member) as inp, destination.open("xb") as out:
                    while True:
                        chunk = inp.read(1024 * 1024)
                        if not chunk:
                            break
                        count += len(chunk)
                        if count > member.file_size:
                            raise ValueError("native member expanded beyond declared bytes")
                        out.write(chunk)
                if count != member.file_size:
                    raise ValueError("native member extracted size mismatch")
                total_written += count
                extracted.add(name)
    if archive_count < 2:
        raise ValueError("official Minecraft 1.8.9 Linux native archive set incomplete")
    sonames = [name for name in extracted if name.endswith(".so")]
    if len(sonames) < 2:
        raise ValueError("expected Linux ELF shared libraries missing")
    print("OFFICIAL_189_LINUX_NATIVE_ARCHIVES_VERIFIED=" + str(archive_count))
    print("OFFICIAL_189_LINUX_NATIVE_FILES_STAGED=" + str(len(extracted)))
    print("OFFICIAL_189_LINUX_NATIVE_BYTES_STAGED=" + str(total_written))
    return native_root


def _download_one_complete_asset(destination_root, sha1, expected_size):
    """Stream one *unique* official object to an exclusive temporary file."""
    path = destination_root / sha1[:2] / sha1
    path.parent.mkdir(parents=True, exist_ok=True)
    url = "https://resources.download.minecraft.net/" + sha1[:2] + "/" + sha1
    if not DIGEST.fullmatch(sha1):
        raise ValueError("invalid asset object digest")
    if not isinstance(expected_size, int) or not (0 <= expected_size <= MAX_COMPLETE_OBJECT):
        raise ValueError("invalid official asset object size")
    if path.exists():
        raise ValueError("asset path unexpectedly existed: " + sha1)
    staging = path.with_name(sha1 + ".partial")
    checksum = hashlib.sha1()
    count = 0
    try:
        # Zero-length blobs are materialized from the uniquely known SHA-1
        # of empty bytes rather than depending on a CDN serving an empty file.
        if expected_size == 0:
            if sha1 != hashlib.sha1(b"").hexdigest():
                raise ValueError("zero-length object hash does not match empty bytes")
            with staging.open("xb"):
                pass
        else:
            with urlopen(
                Request(url, headers={"User-Agent": "customMCclient-189-complete-assets/1"}),
                timeout=90
            ) as response:
                if response.geturl() != url:
                    raise ValueError("unexpected official resource redirect")
                with staging.open("xb") as target:
                    while True:
                        payload = response.read(1024 * 1024)
                        if not payload:
                            break
                        count += len(payload)
                        if count > expected_size or count > MAX_COMPLETE_OBJECT:
                            raise ValueError("asset exceeded pinned size")
                        checksum.update(payload)
                        target.write(payload)
            if count != expected_size or checksum.hexdigest() != sha1:
                raise ValueError("official asset size/hash mismatch: " + sha1)
        staging.replace(path)
        return expected_size
    finally:
        staging.unlink(missing_ok=True)


def _stage_complete_asset_collection(objects, asset_root):
    """Verify every indexed logical asset and every unique content object."""
    unique = {}
    for object_info in objects.values():
        sha1, size = object_info["hash"], object_info["size"]
        if size > MAX_COMPLETE_OBJECT:
            raise ValueError("official object exceeds bounded full-install size")
        previous = unique.setdefault(sha1, size)
        if previous != size:
            raise ValueError("same content hash has conflicting declared sizes")
    total = sum(unique.values())
    if total > MAX_COMPLETE_TOTAL:
        raise ValueError("complete official asset collection exceeds total budget")
    if not unique:
        raise ValueError("complete asset inventory is empty")

    object_root = asset_root / "objects"
    object_root.mkdir()
    # Cap network concurrency, deduplicate identical content hashes and
    # abort on any failed object. The caller removes *all* staging on failure.
    with ThreadPoolExecutor(max_workers=COMPLETE_DOWNLOAD_WORKERS) as pool:
        futures = {
            pool.submit(_download_one_complete_asset, object_root, sha, size): sha
            for sha, size in sorted(unique.items())
        }
        completed = 0
        try:
            for future in as_completed(futures):
                future.result()
                completed += 1
                if completed % 100 == 0:
                    print("OFFICIAL_189_ASSET_DOWNLOAD_PROGRESS=" + str(completed)
                          + "/" + str(len(unique)), flush=True)
        except BaseException:
            for future in futures:
                future.cancel()
            raise
    if completed != len(unique):
        raise ValueError("asset retrieval was incomplete")

    # Perform a final inventory and independent disk-read digest pass.
    for sha, size in unique.items():
        stored = object_root / sha[:2] / sha
        if not stored.is_file() or stored.stat().st_size != size:
            raise ValueError("asset object missing or size changed during staging")
        hasher = hashlib.sha1()
        with stored.open("rb") as reader:
            for block in iter(lambda: reader.read(1024 * 1024), b""):
                hasher.update(block)
        if hasher.hexdigest() != sha:
            raise ValueError("staged asset failed final SHA-1 verification")
    print("OFFICIAL_189_FULL_ASSET_LOGICAL_ENTRIES_VERIFIED=" + str(len(objects)))
    print("OFFICIAL_189_FULL_ASSET_UNIQUE_OBJECTS_VERIFIED=" + str(len(unique)))
    print("OFFICIAL_189_FULL_ASSET_TOTAL_BYTES_VERIFIED=" + str(total))
    print("OFFICIAL_189_FULL_ASSET_INSTALLATION=YES")


def stage_index_and_sampled_assets(metadata, root, full_assets=False):
    info = metadata.get("assetIndex")
    if not isinstance(info, dict) or info.get("id") != "1.8":
        raise ValueError("not the expected Mojang 1.8 asset index")
    sha1, size, url = info.get("sha1"), info.get("size"), info.get("url")
    if not isinstance(url, str) or sha1 != EXPECTED_INDEX_SHA1:
        raise ValueError("unexpected/potentially unpinned Minecraft 1.8 asset index")
    if not url.endswith("/1.8.json") or sha1 not in url:
        raise ValueError("asset index URL is not version/pin bound")
    index = verify_download(
        url, sha1, size, MAX_INDEX_SIZE,
        {"piston-meta.mojang.com", "launchermeta.mojang.com"}
    )
    parsed = json.loads(index)
    objects = parsed.get("objects")
    if not isinstance(objects, dict) or len(objects) != EXPECTED_INDEX_OBJECTS:
        raise ValueError("official 1.8 asset index object count drifted from the pinned 734-entry release")
    for name, obj in objects.items():
        if (not isinstance(name, str) or not name or len(name) > 400
                or name.startswith("/") or "\\" in name
                or "\x00" in name
                or any(part in ("", ".", "..") for part in name.split("/"))):
            raise ValueError("unsafe asset logical path")
        if not isinstance(obj, dict) or not DIGEST.fullmatch(obj.get("hash", "")):
            raise ValueError("asset index contains a malformed object hash")
        if not isinstance(obj.get("size"), int) or obj["size"] < 0:
            raise ValueError("asset index contains invalid object size")
    asset_root = root / "assets"
    indexes = asset_root / "indexes"
    indexes.mkdir(parents=True)
    (indexes / "1.8.json").write_bytes(index)
    if full_assets:
        _stage_complete_asset_collection(objects, asset_root)
        print("OFFICIAL_189_ASSET_INDEX_SHA1_PASS=" + sha1)
        print("OFFICIAL_189_ASSET_INDEX_OBJECTS_VALIDATED=" + str(len(objects)))
        return asset_root

    # Representative deterministic sample, NOT the full set of game assets.
    names = sorted(objects)
    selected = list(dict.fromkeys(names[:6] + names[-6:]))
    sampled = 0
    total = 0
    for name in selected:
        obj = objects[name]
        sha, size = obj["hash"], obj["size"]
        if size > MAX_SAMPLE_OBJECT or total + size > MAX_SAMPLED_BYTES:
            raise ValueError("sample asset exceeds bounded download budget")
        url = "https://resources.download.minecraft.net/" + sha[:2] + "/" + sha
        payload = verify_download(
            url, sha, size, MAX_SAMPLE_OBJECT,
            {"resources.download.minecraft.net"}
        )
        path = asset_root / "objects" / sha[:2] / sha
        path.parent.mkdir(parents=True, exist_ok=True)
        if not path.exists():
            path.write_bytes(payload)
        sampled += 1
        total += size
    if sampled != 12:
        raise ValueError("sample asset inventory was truncated")
    print("OFFICIAL_189_ASSET_INDEX_SHA1_PASS=" + sha1)
    print("OFFICIAL_189_ASSET_INDEX_OBJECTS_VALIDATED=" + str(len(objects)))
    print("OFFICIAL_189_ASSET_OBJECTS_DOWNLOADED_AND_VERIFIED=" + str(sampled))
    print("OFFICIAL_189_FULL_ASSET_INSTALLATION=NO")
    return asset_root


def run(output_directory, full_assets=False):
    root = Path(output_directory).resolve()
    if root.exists() or not root.parent.is_dir():
        raise ValueError("refusing preexisting native/asset staging destination")
    root.mkdir()
    try:
        raw = fetch_bytes(METADATA_URL, METADATA_SHA1, cap=MAX_METADATA)
        metadata = json.loads(raw)
        if (metadata.get("id") != "1.8.9" or
                metadata.get("downloads", {}).get("client", {}).get("sha1")
                != CLIENT_SHA1):
            raise ValueError("pinned Mojang release metadata mismatch")
        stage_natives(metadata, root)
        stage_index_and_sampled_assets(metadata, root, full_assets=full_assets)
        print("OFFICIAL_189_NATIVE_ASSET_PREFLIGHT_PASS=YES")
    except BaseException:
        shutil.rmtree(root)
        raise


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--full-assets", action="store_true",
                        help="verify and stage all 734 pinned official 1.8 asset index entries")
    args = parser.parse_args()
    try:
        run(args.output_dir, full_assets=args.full_assets)
    except Exception as error:
        print("OFFICIAL_189_NATIVE_ASSET_PREFLIGHT_FAILED: " + str(error),
              file=sys.stderr)
        sys.exit(1)
