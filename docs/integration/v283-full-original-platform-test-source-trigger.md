# V283 — comprehensive original-platform test and fixture path coverage

Immutable parent V282 Draft PR #753 `3be02358444a2a9b90d7a0494a177f92b68253b6`.

## Actual confirmed gap

Inspected the complete current Git tree and evaluated the existing original Mojang 1.8.9 `pull_request.paths` filter. Of **244 actual files** under `platform-1.8.9/src/test/java`, **243** matched and one did not:

`platform-1.8.9/src/test/java/net/minecraft/client/main/Main.java`

This is a **test-only alternate-package main-entrypoint stub**, not the pinned authentic Mojang class and not a shipping game implementation. The prior package-only `*Test.java` matcher omits it because its file name is `Main.java` and its package is `net.minecraft.client.main`, not `dev.trexzo.custommc.platform.v1_8_9`. Future nested Java test helper files or fixtures outside the original package/name structure would also be missed. Existing original acceptance/source integrity regressions might thereby change without automatically running real original Mojang binary CI.

V283 adds a single intentionally test-scoped PR filter:

```yaml
- 'platform-1.8.9/src/test/java/**'
```

All existing exact and broad V278–V282 guards remain unchanged. No global repository wildcard, unrelated docs, core test, or launcher test trigger.

Four new deterministic offline Python tests: enumerate **every actual file** in `platform-1.8.9/src/test/java`, assert the known `net.minecraft.client.main.Main` is covered, check future nested and non-`Test.java` fixtures, and prove `core`, `launcher`, `docs`, and README do **not** match. This is a CI scheduling guarantee, not evidence of a real game run.

No JVM production source, Java game/test fixture, runtime implementation, server, X11 client, UUID source assertion, real attack acceptance, 78-step workflow stage, timeout, retry policy, fake PASS marker or original pinned binary is modified.

Certification remains Windows and Ubuntu Foundation 10/10 plus **two independent full actual original Mojang Minecraft 1.8.9 X11 GUI and vanilla server 78/78 runs on identical SHA**. All known genuine V276/V277 native failures remain recorded under issue #503. Stacked Draft only, `main` unchanged, no merge/release.
