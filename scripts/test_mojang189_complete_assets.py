"""Offline regression checks for complete Mojang asset staging: NO network."""
import hashlib
from io import BytesIO
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest
from unittest.mock import patch

import FetchMojang189NativeAssets as assets


class Response(BytesIO):
    def __init__(self, url, content):
        super().__init__(content)
        self.url = url

    def geturl(self):
        return self.url


class CompleteAssetStageTest(unittest.TestCase):
    def test_all_logical_objects_stage_once_per_unique_hash(self):
        first, second = b"test-asset-one", b"second-asset"
        first_hash = hashlib.sha1(first).hexdigest()
        second_hash = hashlib.sha1(second).hexdigest()
        payloads = {first_hash: first, second_hash: second}
        requests = []

        def fake_open(request, timeout=90):
            url = request.full_url
            digest = url.rsplit("/", 1)[-1]
            self.assertEqual(
                "https://resources.download.minecraft.net/" + digest[:2] + "/" + digest,
                url
            )
            requests.append(digest)
            return Response(url, payloads[digest])

        with TemporaryDirectory() as temp:
            root = Path(temp) / "assets"
            root.mkdir()
            objects = {
                "sound/one.ogg": {"hash": first_hash, "size": len(first)},
                "sound/same.ogg": {"hash": first_hash, "size": len(first)},
                "sound/two.ogg": {"hash": second_hash, "size": len(second)},
            }
            with patch.object(assets, "urlopen", side_effect=fake_open):
                assets._stage_complete_asset_collection(objects, root)
            self.assertCountEqual([first_hash, second_hash], requests)
            self.assertEqual(
                first, (root / "objects" / first_hash[:2] / first_hash).read_bytes()
            )
            self.assertEqual(
                second, (root / "objects" / second_hash[:2] / second_hash).read_bytes()
            )
            self.assertFalse(list(root.rglob("*.partial")))

    def test_bad_download_content_never_becomes_verified_object(self):
        declared = hashlib.sha1(b"known-content").hexdigest()
        expected_size = len(b"known-content")
        with TemporaryDirectory() as temp:
            root = Path(temp)
            requested = []
            def fake_open(request, timeout=90):
                requested.append(request.full_url)
                return Response(request.full_url, b"bad--content")
            with patch.object(assets, "urlopen", side_effect=fake_open):
                with self.assertRaisesRegex(ValueError, "size/hash mismatch"):
                    assets._download_one_complete_asset(root, declared, expected_size)
            self.assertEqual(1, len(requested))
            self.assertFalse((root / declared[:2] / declared).exists())
            self.assertFalse((root / declared[:2] / (declared + ".partial")).exists())

    def test_conflicting_sizes_and_unbounded_objects_fail_closed(self):
        sha = hashlib.sha1(b"a").hexdigest()
        with TemporaryDirectory() as temp:
            root = Path(temp)
            root.mkdir()
            with self.assertRaisesRegex(ValueError, "conflicting declared sizes"):
                assets._stage_complete_asset_collection({
                    "a": {"hash": sha, "size": 1},
                    "b": {"hash": sha, "size": 2},
                }, root)
            with self.assertRaisesRegex(ValueError, "bounded full-install size"):
                assets._stage_complete_asset_collection({
                    "oversized": {
                        "hash": sha, "size": assets.MAX_COMPLETE_OBJECT + 1
                    }
                }, root)

    def test_zero_byte_object_uses_known_empty_hash_only(self):
        empty_hash = hashlib.sha1(b"").hexdigest()
        with TemporaryDirectory() as temp:
            root = Path(temp)
            with patch.object(assets, "urlopen", side_effect=AssertionError("network forbidden")):
                self.assertEqual(0, assets._download_one_complete_asset(root, empty_hash, 0))
            self.assertEqual(b"", (root / empty_hash[:2] / empty_hash).read_bytes())
            with self.assertRaisesRegex(ValueError, "zero-length object hash"):
                assets._download_one_complete_asset(root, hashlib.sha1(b"x").hexdigest(), 0)


if __name__ == "__main__":
    unittest.main()
