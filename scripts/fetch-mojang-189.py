#!/usr/bin/env python3
"""Download the genuine Mojang 1.8.9 client into a temporary CI-only path."""
import argparse, hashlib, json, os, sys
from pathlib import Path
from urllib.parse import urlparse
from urllib.request import Request, urlopen

VERSION = "1.8.9"
PIN_SHA1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
PIN_SIZE = 8461484
MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
META_HOSTS = {"piston-meta.mojang.com", "launchermeta.mojang.com"}
DATA_HOSTS = {"piston-data.mojang.com", "launcher.mojang.com"}
MAX_CLIENT = 16 * 1024 * 1024

def fetch(url, allowed, limit):
    if urlparse(url).scheme != "https" or urlparse(url).hostname not in allowed:
        raise ValueError("disallowed download origin: " + url)
    req = Request(url, headers={"User-Agent": "customMCclient-1.8.9-audit/1"})
    with urlopen(req, timeout=90) as resp:
        if urlparse(resp.geturl()).hostname not in allowed:
            raise ValueError("download redirected outside approved Mojang hosts")
        data = resp.read(limit + 1)
    if len(data) > limit:
        raise ValueError("download exceeds size limit")
    return data

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    dest = Path(args.output).resolve()
    if dest.exists() or not dest.parent.is_dir():
        raise ValueError("output must be an absent file in an existing directory")
    versions = json.loads(fetch(MANIFEST, META_HOSTS, 4 * 1024 * 1024))
    matching = [x for x in versions["versions"]
                if x.get("id") == VERSION and x.get("type") == "release"]
    if len(matching) != 1:
        raise ValueError("official version missing/ambiguous")
    entry = matching[0]
    raw = fetch(entry["url"], META_HOSTS, 2 * 1024 * 1024)
    if entry.get("sha1") and hashlib.sha1(raw).hexdigest() != entry["sha1"]:
        raise ValueError("official version metadata hash mismatch")
    metadata = json.loads(raw)
    if metadata.get("id") != VERSION:
        raise ValueError("official version metadata id mismatch")
    info = metadata["downloads"]["client"]
    if info.get("sha1") != PIN_SHA1 or info.get("size") != PIN_SIZE:
        raise ValueError("official 1.8.9 binary hash/size unexpectedly changed")
    data = fetch(info["url"], DATA_HOSTS, MAX_CLIENT)
    if len(data) != PIN_SIZE or hashlib.sha1(data).hexdigest() != PIN_SHA1:
        raise ValueError("downloaded official 1.8.9 binary integrity mismatch")
    try:
        with dest.open("xb") as out:
            out.write(data)
    except Exception:
        dest.unlink(missing_ok=True)
        raise
    print("OFFICIAL_189_CLIENT_SHA1_PASS=" + PIN_SHA1)
    print("OFFICIAL_189_CLIENT_BYTES=" + str(len(data)))
    print("EPHEMERAL_ONLY=YES")

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        sys.exit("OFFICIAL_189_DOWNLOAD_FAILED: " + str(exc))
