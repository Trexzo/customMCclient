# V309 — Original M417 four-sentinel admission requires distinct new UUIDs AND net +4

**Parent immutable double-certified V307**: `2539c81f7f5b856189f0227f27b5b28f4cfc089a`.
[PR #781](https://github.com/Trexzo/customMCclient/pull/781): Windows+Ubuntu Foundation 20/20,
two independent original Minecraft 1.8.9 GUI/X11 + vanilla-server 78/78 PASS:
[attempt 1](https://github.com/Trexzo/customMCclient/actions/runs/39987763047/attempts/1),
[attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39987763047/attempts/2).
V307 stays unchanged; these are **historical certificates, not inherited by V309**.

## Scoped out-of-chain source audit

[Issue #777](https://github.com/Trexzo/customMCclient/issues/777) distinguishes
already-ancestral PRs from truly divergent branches.
[PR #526](https://github.com/Trexzo/customMCclient/pull/526)
is a divergent alternative whose source-native nonplayer UUID witness was
not integrated as a required M417 positive predicate. V307's V273 audit
already computes genuinely new nonplayer UUIDs and logs
`CUSTOMMC_189_REJOIN_FOUR_NOVEL_NATIVE_UUID_DIAG=YES`, yet its
`CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS=YES` gate
only checked a four-entity NET count increase. An increase alone cannot
identify which actual nonplayer UUIDs arrived.

## Strictly additive acceptance-only change

Use V307's existing UUID-set snapshot helper as an **additional required**
predicate. A genuine original source snapshot must have **both**:
1. Original net nonplayer count at least baseline + 4, using safe widened arithmetic;
2. At least four distinct nonplayer UUIDs absent from the B-only baseline.

Require the same real original A-absent / B-selected / locked UUID conditions as
before; no extra teleport, synthetic World entities, replacement counter, packet,
player source or acceptance workaround. Source diagnostics remain read-only and
never print a genuine PASS by themselves. The normal true Mojang test must still
observe real server-generated ArmorStands, native World+UUID, controller, ray,
damage, and every original 78 workflow stage.

Three new adversarial source test methods verify positive net+novel, net-only
rejection, novel-only rejection, player UUID exclusion, index reshuffle,
invalid baseline, and overflow. These are unit tests, **not certification**.

## Certification boundary

This changes only **test acceptance** Java and its regression plus this note;
production Minecraft behavior, native timeout, gate counts, CI workflow and
launcher/build files unchanged. New exact-head Foundation Windows+Ubuntu
10/10 each and TWO full independent original Mojang 1.8.9 78/78 runs are
mandatory before declaring V309 double-certified. Preserve all previous FAILs,
keep PR Draft and main untouched; no merge or release.
