#!/usr/bin/env python3
"""Ephemeral, checksum-pinned Mojang 1.8.9 client for real-class mapping preflight.

This program never adds the copyrighted client binary to Git, test artifacts, or
logs; the file is private to the transient CI runner and explicitly deleted.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import sys
from urllib.parse import urlparse
from urllib.request import Request, urlopen

VERSION = "1.8.9"
CLIENT_SHA1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
CLIENT_SIZE = 8461484
MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
META_HOSTS = {"piston-meta.mojang.com", "launchermeta.mojang.com"}
CLIENT_HOSTS = {"piston-data.mojang.com", "launcher.mojang.com"}
LIMIT = 16 * 1024 * 1024


def checked_url(url, hosts):
    parsed = urlparse(url)
    if parsed.scheme != "https" or parsed.hostname not in hosts:
        raise ValueError("non-Mojang or non-HTTPS download URL: " + str(url))
    return url


def request_bytes(url, limit, hosts):
    checked_url(url, hosts)
    req = Request(url, headers={"User-Agent": "customMCclient-real-189-preflight/1"})
    with urlopen(req, timeout=60) as response:
        final_url = response.geturl()
        checked_url(final_url, hosts)
        data = response.read(limit + 1)
    if len(data) > limit:
        raise ValueError("Mojang metadata exceeds preflight cap")
    return data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, help="Transient CI-only JAR path")
    args = parser.parse_args()
    output = Path(args.output).resolve()
    if output.exists():
        raise ValueError("refusing to overwrite pre-existing JAR")
    if not output.parent.is_dir():
        raise ValueError("output directory missing")

    manifest = json.loads(request_bytes(MANIFEST_URL, 4 * 1024 * 1024, META_HOSTS))
    matches = [item for item in manifest["versions"] if item.get("id") == VERSION]
    if len(matches) != 1 or matches[0].get("type") != "release":
        raise ValueError("missing or ambiguous official 1.8.9 release")
    entry = matches[0]
    raw_metadata = request_bytes(entry["url"], 2 * 1024 * 1024, META_HOSTS)
    if entry.get("sha1"):
        actual = hashlib.sha1(raw_metadata).hexdigest()
        if actual.lower() != entry["sha1"].lower():
            raise ValueError("official version metadata SHA1 mismatch")
    metadata = json.loads(raw_metadata)
    if metadata.get("id") != VERSION:
        raise ValueError("download metadata id mismatch")
    client = metadata["downloads"]["client"]
    if client.get("sha1", "").lower() != CLIENT_SHA1 or client.get("size") != CLIENT_SIZE:
        raise ValueError("Mojang 1.8.9 expected client hash/size changed")
    url = checked_url(client["url"], CLIENT_HOSTS)

    temp = output.with_name(output.name + ".partial")
    if temp.exists():
        raise ValueError("refusing to overwrite stale partial JAR")
    h = hashlib.sha1()
    count = 0
    try:
        req = Request(url, headers={"User-Agent": "customMCclient-real-189-preflight/1"})
        with urlopen(req, timeout=120) as response:
            checked_url(response.geturl(), CLIENT_HOSTS)
            with temp.open("xb") as stream:
                while True:
                    data = response.read(1024 * 1024)
                    if not data:
                        break
                    count += len(data)
                    if count > LIMIT:
                        raise ValueError("download exceeds cap")
                    h.update(data)
                    stream.write(data)
        actual = h.hexdigest()
        if actual != CLIENT_SHA1 or count != CLIENT_SIZE:
            raise ValueError("Mojang client integrity failure: sha1=" + actual +
                             " bytes=" + str(count))
        os.replace(str(temp), str(output))
    finally:
        if temp.exists():
            temp.unlink()
    print("MOJANG_189_CLIENT_SHA1_PASS=" + CLIENT_SHA1)
    print("MOJANG_189_CLIENT_SIZE_PASS=" + str(CLIENT_SIZE))
    print("EPHEMERAL_CLIENT_ONLY=YES")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("OFFICIAL_189_CLIENT_PREFLIGHT_FAILED: " + str(error), file=sys.stderr)
        sys.exit(1)
