#!/usr/bin/env python3
"""Fetch official Minecraft 1.8.9 dedicated server, pinned by release metadata.

The proprietary JAR exists only in the transient CI runner. Never archive,
check in, or publish the JAR; the localhost test uses it offline.
"""
import argparse
import hashlib
import json
from pathlib import Path
import sys
from urllib.parse import urlsplit
from urllib.request import Request, urlopen

from FetchMojang189Libraries import (
    CLIENT_SHA1, METADATA_SHA1, METADATA_URL, MAX_METADATA, fetch_bytes
)

EXPECTED_SHA1 = "b58b2ceb36e01bcd8dbf49c8fb66c55a9f0676cd"
EXPECTED_SIZE = 8320755
MAX_JAR_BYTES = 16 * 1024 * 1024
ALLOWED_ENDPOINT = (
    "https://piston-data.mojang.com/v1/objects/"
    + EXPECTED_SHA1 + "/server.jar"
)


def fetch(output):
    target = Path(output).resolve()
    if target.exists() or not target.parent.is_dir():
        raise ValueError("refusing existing output or nonexistent directory")
    metadata = json.loads(fetch_bytes(
        METADATA_URL, METADATA_SHA1, cap=MAX_METADATA
    ))
    if (metadata.get("id") != "1.8.9"
            or metadata.get("downloads", {}).get("client", {}).get("sha1")
            != CLIENT_SHA1):
        raise ValueError("wrong official Minecraft version metadata")
    server = metadata.get("downloads", {}).get("server")
    if not isinstance(server, dict):
        raise ValueError("official version has no dedicated server metadata")
    if (server.get("sha1") != EXPECTED_SHA1
            or server.get("size") != EXPECTED_SIZE):
        raise ValueError("pinned dedicated server hash/size changed")
    original = server.get("url")
    if not isinstance(original, str):
        raise ValueError("official dedicated server URL missing")
    parsed = urlsplit(original)
    # Mojang 1.8.9 metadata preserves its legacy launcher.mojang.com URL,
    # but the current official immutable Mojang object CDN serves this exact
    # same SHA1-named binary. No arbitrary redirects or external hosts.
    if (parsed.scheme != "https"
            or parsed.hostname not in (
                "launcher.mojang.com", "piston-data.mojang.com")
            or EXPECTED_SHA1 not in parsed.path):
        raise ValueError("unexpected 1.8.9 release server URL")
    partial = target.with_name(target.name + ".partial")
    if partial.exists():
        raise ValueError("stale partial server download")
    digest = hashlib.sha1()
    total = 0
    try:
        with urlopen(Request(ALLOWED_ENDPOINT, headers={
            "User-Agent": "customMCclient-189-server-preflight/1"
        }), timeout=120) as response:
            if response.geturl() != ALLOWED_ENDPOINT:
                raise ValueError("Mojang server download redirected")
            with partial.open("xb") as out:
                while True:
                    data = response.read(1024 * 1024)
                    if not data:
                        break
                    total += len(data)
                    if total > MAX_JAR_BYTES:
                        raise ValueError("Mojang server exceeds download cap")
                    digest.update(data)
                    out.write(data)
        if total != EXPECTED_SIZE or digest.hexdigest() != EXPECTED_SHA1:
            raise ValueError("Mojang dedicated server size/SHA-1 mismatch")
        partial.replace(target)
    finally:
        partial.unlink(missing_ok=True)
    print("OFFICIAL_189_DEDICATED_SERVER_SHA1_PASS=" + EXPECTED_SHA1)
    print("OFFICIAL_189_DEDICATED_SERVER_BYTES_PASS=" + str(total))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    try:
        fetch(args.output)
    except Exception as error:
        print("OFFICIAL_189_SERVER_DOWNLOAD_FAILED: " + str(error),
              file=sys.stderr)
        sys.exit(1)
