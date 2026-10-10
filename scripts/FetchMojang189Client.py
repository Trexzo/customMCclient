#!/usr/bin/env python3
"""Fetch a transient, checksum-pinned official Mojang 1.8.9 client JAR.

Never commit, print contents of, or upload the proprietary JAR to an artifact.
"""
import argparse
import hashlib
import os
from pathlib import Path
import sys
from urllib.request import Request, urlopen

SHA1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
SIZE = 8461484
URL = "https://piston-data.mojang.com/v1/objects/" + SHA1 + "/client.jar"
MAX_BYTES = 16 * 1024 * 1024


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    target = Path(args.output).resolve()
    if target.exists() or not target.parent.is_dir():
        raise ValueError("refusing to overwrite input or write to missing directory")
    temporary = target.with_name(target.name + ".partial")
    if temporary.exists():
        raise ValueError("stale partial file exists")
    checksum = hashlib.sha1()
    count = 0
    try:
        req = Request(URL, headers={"User-Agent": "customMCclient-real189-preflight/1"})
        with urlopen(req, timeout=120) as response:
            if response.geturl() != URL:
                raise ValueError("Mojang download redirected unexpectedly")
            with temporary.open("xb") as output:
                while True:
                    part = response.read(1024 * 1024)
                    if not part:
                        break
                    count += len(part)
                    if count > MAX_BYTES:
                        raise ValueError("file exceeds expected safety cap")
                    checksum.update(part)
                    output.write(part)
        if count != SIZE or checksum.hexdigest() != SHA1:
            raise ValueError("official Mojang 1.8.9 client checksum/size mismatch")
        os.replace(str(temporary), str(target))
        print("OFFICIAL_189_CLIENT_SHA1_PASS=" + SHA1)
        print("OFFICIAL_189_CLIENT_SIZE_PASS=" + str(count))
        print("DOWNLOAD_RESTRICTED_TO_OFFICIAL_CDN=YES")
    finally:
        if temporary.exists():
            temporary.unlink()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("OFFICIAL_189_CLIENT_FETCH_FAILED: " + str(error), file=sys.stderr)
        sys.exit(1)
