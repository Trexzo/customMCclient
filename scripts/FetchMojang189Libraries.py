#!/usr/bin/env python3
"""Fetch transient, checksum-verified *managed* Linux libraries for Mojang 1.8.9.

This does not fetch assets, natives, or account credentials. Nothing is checked
into Git or uploaded as a workflow artifact. The official signed client is
fetched by FetchMojang189Client.py separately.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sys
from urllib.parse import urlsplit
from urllib.request import Request, urlopen

METADATA_SHA1 = "d546f1707a3f2b7d034eece5ea2e311eda875787"
METADATA_URL = (
    "https://piston-meta.mojang.com/v1/packages/"
    + METADATA_SHA1 + "/1.8.9.json"
)
CLIENT_SHA1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
MAX_METADATA = 512 * 1024
MAX_LIBRARY = 32 * 1024 * 1024
MAX_TOTAL = 256 * 1024 * 1024
DIGEST = re.compile(r"^[0-9a-f]{40}$")
SAFE_PATH = re.compile(r"^[A-Za-z0-9._/-]+$")


def fetch_bytes(url, checksum, size=None, cap=MAX_LIBRARY):
    if not DIGEST.fullmatch(checksum):
        raise ValueError("download is missing a valid SHA-1")
    parts = urlsplit(url)
    allowed = (
        parts.scheme == "https"
        and not parts.username
        and not parts.password
        and not parts.port
        and not parts.query
        and not parts.fragment
        and (
            (parts.hostname == "piston-meta.mojang.com" and url == METADATA_URL)
            or (parts.hostname == "libraries.minecraft.net"
                and parts.path.startswith("/"))
        )
    )
    if not allowed:
        raise ValueError("library/metadata URL is not an allowed official endpoint")
    if size is not None and (not isinstance(size, int) or size <= 0 or size > cap):
        raise ValueError("invalid declared download size")
    request = Request(url, headers={"User-Agent": "customMCclient-189-runtime-acceptance/1"})
    count = 0
    checksum_actual = hashlib.sha1()
    result = bytearray()
    with urlopen(request, timeout=90) as response:
        if response.geturl() != url:
            raise ValueError("unexpected redirect away from official URL")
        while True:
            chunk = response.read(1024 * 1024)
            if not chunk:
                break
            count += len(chunk)
            if count > cap:
                raise ValueError("download exceeds capped size")
            checksum_actual.update(chunk)
            result.extend(chunk)
    if checksum_actual.hexdigest() != checksum or (size is not None and count != size):
        raise ValueError("official artifact hash/size mismatch")
    return bytes(result)


def allowed_on_linux(library):
    rules = library.get("rules")
    if not rules:
        return True
    allowed = False
    for rule in rules:
        os_rule = rule.get("os", {})
        name = os_rule.get("name")
        if name is not None and name != "linux":
            continue
        arch = os_rule.get("arch")
        if arch is not None and arch not in ("x86_64", "amd64"):
            continue
        if os_rule.get("version") is not None:
            raise ValueError("unimplemented version-specific OS rule: fail closed")
        if rule.get("features"):
            # No optional launcher features are enabled by this probe.
            continue
        action = rule.get("action")
        if action not in ("allow", "disallow"):
            raise ValueError("invalid Mojang library rule action")
        allowed = action == "allow"
    return allowed


def run(destination):
    root = Path(destination).resolve()
    if root.exists() or not root.parent.is_dir():
        raise ValueError("refusing existing output or nonexistent parent")
    root.mkdir()
    try:
        raw_metadata = fetch_bytes(
            METADATA_URL, METADATA_SHA1, cap=MAX_METADATA
        )
        metadata = json.loads(raw_metadata)
        if metadata.get("id") != "1.8.9":
            raise ValueError("unexpected Minecraft version metadata")
        if metadata.get("downloads", {}).get("client", {}).get("sha1") != CLIENT_SHA1:
            raise ValueError("metadata does not match pinned genuine client")
        if metadata.get("mainClass") != "net.minecraft.client.main.Main":
            raise ValueError("unexpected official main class")

        libraries = metadata.get("libraries")
        if not isinstance(libraries, list):
            raise ValueError("no official libraries in pinned metadata")
        classpath = []
        total = 0
        for library in libraries:
            if not allowed_on_linux(library):
                continue
            downloads = library.get("downloads", {})
            artifact = downloads.get("artifact")
            if artifact is None:
                if "natives" in library:
                    # Native classifiers are a separate graphical-startup gate.
                    continue
                raise ValueError("managed library is missing download metadata")
            relative = artifact.get("path", "")
            if (not SAFE_PATH.fullmatch(relative)
                    or relative.startswith("/")
                    or any(part in (".", "..") for part in relative.split("/"))
                    or not relative.endswith(".jar")):
                raise ValueError("unsafe official library path")
            url = artifact.get("url")
            if url != "https://libraries.minecraft.net/" + relative:
                raise ValueError("unexpected official library download location")
            sha1 = artifact.get("sha1")
            size = artifact.get("size")
            if not isinstance(size, int) or size <= 0 or size > MAX_LIBRARY:
                raise ValueError("invalid library size")
            total += size
            if total > MAX_TOTAL:
                raise ValueError("total runtime library size exceeds budget")
            name = "{:03d}-{}-{}".format(
                len(classpath), sha1[:12], Path(relative).name
            )
            output = root / name
            payload = fetch_bytes(url, sha1, size=size)
            with output.open("xb") as stream:
                stream.write(payload)
            classpath.append(str(output))

        if len(classpath) < 12:
            raise ValueError("official library set is unexpectedly incomplete")
        if not any("jopt-simple" in path for path in classpath):
            raise ValueError("official JOpt Simple dependency absent")
        (root / "1.8.9.json").write_bytes(raw_metadata)
        (root / "classpath.txt").write_text(
            "\n".join(classpath) + "\n", encoding="utf-8"
        )
        print("OFFICIAL_189_METADATA_SHA1_PASS=" + METADATA_SHA1)
        print("OFFICIAL_189_MANAGED_LIBRARIES_VERIFIED=" + str(len(classpath)))
        print("OFFICIAL_189_LIBRARY_BYTES_VERIFIED=" + str(total))
        print("OFFICIAL_189_LIBRARY_CLASSPATH=" + str(root / "classpath.txt"))
        print("CLIENT_ASSETS_AND_NATIVES_NOT_DOWNLOADED=YES")
    except BaseException:
        shutil.rmtree(root)
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", required=True)
    args = parser.parse_args()
    run(args.output_dir)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("OFFICIAL_189_LIBRARIES_FETCH_FAILED: " + str(error),
              file=sys.stderr)
        sys.exit(1)
