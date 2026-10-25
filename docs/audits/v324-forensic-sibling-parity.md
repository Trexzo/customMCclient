# Source-parity audit: divergent forensic sibling PRs #796–#799

**Date:** 2026-10-25. **Scope:** original Minecraft 1.8.9 failure-only observability. Snapshot target is immutable certified **V324 `060445b541f7b83b975d3311683e7778a431bf0e`**. V325 `55aef8aeaae76b3345c1e03c7bf8d21aaf1d8540` changes other files (M417, a rejoin test and a new Python regression) and inherits these unchanged diagnostics. This audit is **source parity, not equivalence of original Git histories or a new gameplay certificate**.

## Method

1. Re-fetched the original open Draft PR patch bodies and exact head SHAs:
   - #796 `a33def99834c7043fd35896d03bf27f378c6d8a8`
   - #797 `26301aeafdbfb3843c146b9e51b2f8c2b066e84a`
   - #798 `80b73843abfa18404799626c62a06c91d491bef2`
   - #799 `50ee2714d0afd7af97f8d216f27c94b821303ffe`.
2. Inspected complete, untruncated Git trees for those four heads and V325, compared blob object SHAs for each donor-touched path, and checked all meaningful added lines from the donor patches against the current full source at corresponding paths. Blank lines and comments were excluded from the added-line inclusion count; code from other branches may coexist in the destination, so byte-identical files are distinguished from preserved added lines.
3. Reviewed the one intentionally rewritten shared regression `scripts/test_mojang189_v317_early_b_failure_source.py`. Source assertions now require **four independent fail-only diagnostic `try` blocks**, each exception-bounded, instead of the conflicting historical sibling assertions for exactly two. The underlying strict native B=0 failure is still bare-reraised. Original V324 `2×80/80` verified the combined behavior, and separate V325 CI is independently pending/verified only as separately stated in its PR.

## Individual source comparisons

| Historical PR | Code/test paths | Direct source finding | Unresolved nuance |
|---|---:|---|---|
| **#796** | 3 | All meaningful donor-added code lines are present in current V324/V325. The dedicated original post-A timeline Python test file is **byte-for-byte identical at Git blob SHA**. | Shared Java observer and CI harness have later additions; whole-file bytes differ. |
| **#797** | 5 | The original B `hurtTime` helper/observer and bounded JVM log source lines remain. Dedicated Python B-hurt test file is **Git-blob-identical**. | Shared V317 `inner_tries==2` assertion was **superseded** by `inner_tries==4`, not dropped. Other source paths were extended. |
| **#798** | 3 | Real vanilla `stat.damageDealt`/scoreboard failure-only observer lines remain. Dedicated original server-damage Python test file is **Git-blob-identical**. | Shared V317 two-diagnostic exception test was superseded by four independent handlers; no assertion of identical old test text. |
| **#799** | 6 | Original Mojang `attackEntity` argument UUID provenance observer retained. `Minecraft189RuntimeBridge.java`, `Minecraft189SimultaneousControllerProvenanceTest.java` and the dedicated Python controller-provenance test are **byte-identical** to this PR's head. | Shared SimultaneousTargetAcceptance, the CI smoke harness, and the V317 failure audit have additional combined logic. |

**Quantitative added-line evidence (nonblank, noncomment):** all selected added lines from #796's three paths appear in current source; for #797 all except the obsolete exact-two diagnostic-test assertion and associated older test-source arrangements appear; #798 similarly replaces its exact-two exception inventory with the comprehensive four-diagnostic regression; #799 retains all additions except the older exact-two shared regression test assertions. All donor paths exist in the certified target tree.

## Safety and conclusion

- These four **Git-diverged** siblings are **source-superseded for their identified diagnostics**; automatic cherry-picking is inappropriate and could revert newer checks.
- Native attack authorization is **not** provided by these reporters: original hit permission, strict server B=0, correct UUID owner/ray/FOV/Wall checks, original World source, M419 margins and Scaffold negative cases remain independent fail-closed gates. A diagnostic exception must not mask an earlier failure.
- This review does **not** claim comprehensive semantic parity for the original donor tree, transitive test behaviors, or historical binary provenance. It demonstrates direct inclusion of their meaningful additions, complete-file identity where available, and intentional stricter replacement of the shared exception-count test.
- #782/#783 revealed a **different genuine missing M417 acceptance condition**. That gap is addressed separately in [Draft V325 PR #806](https://github.com/Trexzo/customMCclient/pull/806). V325's own original binary CI must pass independently; V324's double certificate cannot be inherited.
- All original PRs remain OPEN Draft for recordkeeping; no merge, PR closure, rebase, retarget or release.
