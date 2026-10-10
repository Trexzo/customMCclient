#!/usr/bin/env python3
"""Verify and stage transient Mojang 1.8.9 Linux natives and bounded 1.8 assets.

This is NOT a complete asset installation or graphical Minecraft launch.
All downloads come from Mojang URLs with pinned SHA-1 and bounded size.
No binaries or retrieved asset data are stored in Git or uploaded as artifacts.
"""
import argparse
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


def stage_index_and_sampled_assets(metadata, root):
    info = metadata.get("assetIndex")
    if not isinstance(info, dict) or info.get("id") != "1.8":
        raise ValueError("not the expected Mojang 1.8 asset index")
    sha1, size, url = info.get("sha1"), info.get("size"), info.get("url")
    if not isinstance(url, str) or not DIGEST.fullmatch(sha1 or ""):
        raise ValueError("asset index metadata is missing SHA-1/URL")
    if not url.endswith("/1.8.json") or sha1 not in url:
        raise ValueError("asset index URL is not version/pin bound")
    index = verify_download(
        url, sha1, size, MAX_INDEX_SIZE,
        {"piston-meta.mojang.com", "launchermeta.mojang.com"}
    )
    parsed = json.loads(index)
    objects = parsed.get("objects")
    if not isinstance(objects, dict) or not (100 <= len(objects) <= MAX_ASSET_OBJECTS):
        raise ValueError("pinned 1.8 asset index object count outside bounded range: "
                         + str(len(objects) if isinstance(objects, dict) else "missing"))
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


def run(output_directory):
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
        stage_index_and_sampled_assets(metadata, root)
        print("OFFICIAL_189_NATIVE_ASSET_PREFLIGHT_PASS=YES")
    except BaseException:
        shutil.rmtree(root)
        raise


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", required=True)
    args = parser.parse_args()
    try:
        run(args.output_dir)
    except Exception as error:
        print("OFFICIAL_189_NATIVE_ASSET_PREFLIGHT_FAILED: " + str(error),
              file=sys.stderr)
        sys.exit(1)
