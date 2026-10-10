#!/usr/bin/env python3
"""Download and verify Mojang's official 1.8.9 client only in ephemeral CI."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import sys
from urllib.parse import urlparse
from urllib.request import Request, urlopen

EXPECTED_SHA1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
EXPECTED_SIZE = 8461484
VERSION = "1.8.9"
MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
META_HOSTS = {"piston-meta.mojang.com", "launchermeta.mojang.com"}
DATA_HOSTS = {"piston-data.mojang.com", "launcher.mojang.com"}
MAX_JAR = 16 * 1024 * 1024

def checked_url(url, hosts):
    parsed = urlparse(url)
    if parsed.scheme != "https" or parsed.hostname not in hosts:
        raise ValueError("Not an approved Mojang HTTPS URL: " + str(url))
    return url

def download_metadata(url, hosts, limit=4 * 1024 * 1024):
    req = Request(checked_url(url, hosts), headers={"User-Agent": "customMCclient-189-preflight/1"})
    with urlopen(req, timeout=65) as response:
        checked_url(response.geturl(), hosts)
        raw = response.read(limit + 1)
    if len(raw) > limit:
        raise ValueError("Mojang metadata exceeds size cap")
    return raw

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    output = Path(args.output).resolve()
    if not output.parent.is_dir() or output.exists():
        raise ValueError("Destination must be in existing directory and not yet exist")
    manifest = json.loads(download_metadata(MANIFEST, META_HOSTS))
    versions = [v for v in manifest["versions"] if v.get("id") == VERSION]
    if len(versions) != 1 or versions[0].get("type") != "release":
        raise ValueError("Unexpected 1.8.9 metadata")
    item = versions[0]
    raw_info = download_metadata(item["url"], META_HOSTS)
    if item.get("sha1") and hashlib.sha1(raw_info).hexdigest() != item["sha1"].lower():
        raise ValueError("Official version metadata checksum mismatch")
    info = json.loads(raw_info)
    if info.get("id") != VERSION:
        raise ValueError("Version JSON mismatch")
    client = info["downloads"]["client"]
    if client.get("sha1", "").lower() != EXPECTED_SHA1 or client.get("size") != EXPECTED_SIZE:
        raise ValueError("Official 1.8.9 client checksum/size differs from pinned value")
    req = Request(checked_url(client["url"], DATA_HOSTS),
                  headers={"User-Agent": "customMCclient-189-preflight/1"})
    partial = output.with_suffix(output.suffix + ".partial")
    if partial.exists():
        raise ValueError("Stale download partial exists")
    sha1 = hashlib.sha1()
    size = 0
    try:
        with urlopen(req, timeout=120) as response:
            checked_url(response.geturl(), DATA_HOSTS)
            with partial.open("xb") as stream:
                while True:
                    chunk = response.read(1024 * 1024)
                    if not chunk:
                        break
                    size += len(chunk)
                    if size > MAX_JAR:
                        raise ValueError("Client download exceeded size cap")
                    sha1.update(chunk)
                    stream.write(chunk)
        if sha1.hexdigest() != EXPECTED_SHA1 or size != EXPECTED_SIZE:
            raise ValueError("Actual Mojang binary did not match SHA1 and size")
        os.replace(str(partial), str(output))
    finally:
        if partial.exists():
            partial.unlink()
    print("MOJANG_189_CLIENT_SHA1_PASS=" + EXPECTED_SHA1)
    print("MOJANG_189_CLIENT_SIZE_PASS=" + str(EXPECTED_SIZE))
    print("EPHEMERAL_JAR_ONLY=YES")

if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("MOJANG_189_DOWNLOAD_FAILED: " + str(error), file=sys.stderr)
        sys.exit(1)
