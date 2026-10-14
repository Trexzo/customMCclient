"""Offline regression checks for complete Mojang asset staging: NO network."""
import hashlib
from urllib.error import HTTPError, URLError
from io import BytesIO
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest
from unittest.mock import patch

import FetchMojang189NativeAssets as assets
import FetchMojang189Libraries as libraries


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


class CompleteAssetTransportRetryTest(unittest.TestCase):
    """No network: independently test full official asset object retries."""

    def test_midstream_connection_loss_cleans_partial_and_verifies_retry(self):
        content = b"official-pinned-object"
        sha = hashlib.sha1(content).hexdigest()

        class BrokenAfterFirstChunk(Response):
            def __init__(self, url):
                super().__init__(url, content)
                self.read_calls = 0

            def read(self, size=-1):
                self.read_calls += 1
                if self.read_calls == 1:
                    return super().read(3)
                raise URLError(OSError(101, "Network is unreachable"))

        with TemporaryDirectory() as temp:
            root = Path(temp)
            final = root / sha[:2] / sha
            partial = final.with_name(sha + ".partial")
            calls = []

            def fake_open(request, timeout=90):
                url = request.full_url
                self.assertEqual(
                    "https://resources.download.minecraft.net/" + sha[:2] + "/" + sha,
                    url)
                self.assertFalse(partial.exists(),
                                 "interrupted bytes must be removed before retry")
                calls.append(url)
                return (BrokenAfterFirstChunk(url) if len(calls) == 1
                        else Response(url, content))

            with patch.object(assets, "urlopen", side_effect=fake_open), \
                    patch.object(assets.time, "sleep") as sleep:
                self.assertEqual(len(content),
                                 assets._download_one_complete_asset(
                                     root, sha, len(content)))
            self.assertEqual(2, len(calls))
            sleep.assert_called_once_with(1)
            self.assertEqual(content, final.read_bytes())
            self.assertFalse(partial.exists())

    def test_repeated_network_failure_never_commits_unverified_asset(self):
        content = b"never-downloaded"
        sha = hashlib.sha1(content).hexdigest()
        with TemporaryDirectory() as temp:
            root = Path(temp)
            with patch.object(assets, "urlopen",
                              side_effect=URLError(OSError(101, "Network is unreachable"))) as opened, \
                    patch.object(assets.time, "sleep") as sleep:
                with self.assertRaises(URLError):
                    assets._download_one_complete_asset(root, sha, len(content))
            self.assertEqual(3, opened.call_count)
            self.assertEqual([((1,), {}), ((3,), {})],
                             [(x.args, x.kwargs) for x in sleep.call_args_list])
            self.assertFalse((root / sha[:2] / sha).exists())
            self.assertFalse((root / sha[:2] / (sha + ".partial")).exists())

    def test_http_rejection_is_not_retried_or_accepted(self):
        content = b"correct-asset"
        sha = hashlib.sha1(content).hexdigest()
        url = "https://resources.download.minecraft.net/" + sha[:2] + "/" + sha
        with TemporaryDirectory() as temp:
            root = Path(temp)
            with patch.object(assets, "urlopen",
                              side_effect=HTTPError(url, 403, "Forbidden", None, None)) as opened, \
                    patch.object(assets.time, "sleep") as sleep:
                with self.assertRaises(HTTPError):
                    assets._download_one_complete_asset(root, sha, len(content))
            self.assertEqual(1, opened.call_count)
            sleep.assert_not_called()
            self.assertFalse((root / sha[:2] / sha).exists())



class PinnedLibraryTransportRetryTest(unittest.TestCase):
    """Offline: transient routing errors never bypass official download pins."""

    URL = "https://libraries.minecraft.net/custommc/test-fixture.jar"

    def test_transient_transport_recovers_with_fresh_verified_digest(self):
        correct = b"pinned-library-payload"
        digest = hashlib.sha1(correct).hexdigest()
        attempts = []

        def fake_open(request, timeout=90):
            attempts.append(request.full_url)
            if len(attempts) <= 2:
                raise URLError(OSError(101, "Network is unreachable"))
            return Response(request.full_url, correct)

        with patch.object(libraries, "urlopen", side_effect=fake_open), \
                patch.object(libraries.time, "sleep") as sleep:
            self.assertEqual(correct, libraries.fetch_bytes(
                self.URL, digest, size=len(correct)))
        self.assertEqual([self.URL] * 3, attempts)
        self.assertEqual([((1,), {}), ((3,), {})],
                         [(call.args, call.kwargs) for call in sleep.call_args_list])

    def test_exhausted_transport_retries_rethrow_and_never_accept(self):
        digest = hashlib.sha1(b"correct").hexdigest()
        with patch.object(libraries, "urlopen",
                          side_effect=URLError(OSError(101, "Network is unreachable"))) as opened, \
                patch.object(libraries.time, "sleep") as sleep:
            with self.assertRaises(URLError):
                libraries.fetch_bytes(self.URL, digest)
        self.assertEqual(3, opened.call_count)
        self.assertEqual(2, sleep.call_count)

    def test_hash_mismatch_and_http_rejection_never_retry(self):
        digest = hashlib.sha1(b"good").hexdigest()
        with patch.object(libraries, "urlopen",
                          return_value=Response(self.URL, b"evil")) as opened, \
                patch.object(libraries.time, "sleep") as sleep:
            with self.assertRaisesRegex(ValueError, "hash/size mismatch"):
                libraries.fetch_bytes(self.URL, digest, size=4)
        self.assertEqual(1, opened.call_count)
        sleep.assert_not_called()

        forbidden = HTTPError(self.URL, 403, "Forbidden", None, None)
        with patch.object(libraries, "urlopen", side_effect=forbidden) as opened, \
                patch.object(libraries.time, "sleep") as sleep:
            with self.assertRaises(HTTPError):
                libraries.fetch_bytes(self.URL, digest)
        self.assertEqual(1, opened.call_count)
        sleep.assert_not_called()

if __name__ == "__main__":
    unittest.main()
