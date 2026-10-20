# V263 — original local-player Armor native source ownership and transactional durability

Parent V262 [Draft PR #733](https://github.com/Trexzo/customMCclient/pull/733), exact source `9bd55927041bfa96b5f5e237806897afd8a79658`.

## Concrete source gaps
`Minecraft189HostRuntime.playerArmor` formerly read eight separate original local-player mapped armor getter callbacks (four equipment flags, four armor ItemStack references) before publishing a HUD snapshot **without** any exact source identity protection. Synchronous getter callbacks can reenter HostRuntime with a changed/null original local player and then return old values, producing mixed or stale original-player evidence. Later durability access (`customMcItemDamage`/`customMcMaxDamage` on each original ItemStack, during `Minecraft189PlayerArmorState.update`) could also synchronously change the source. Native RuntimeException/Error or invalid negative original durability left the old valid host snapshot intact.

Separately, the armor state `update` wrote the new equipment mask *before* calling item getters, so throwing/invalid original damage could expose a changed mask with old durability while the previously available state remained true. This is a concrete partial-write bug independently of the source lease.

## Implementation
Use typed V251 `Minecraft189ExactSourceLease<Minecraft189PlayerArmorAccess>` owner, checked after **each of 8 genuine native armor getters** and again after the actual item-damage/max-damage reads. Nested same original does not double-sample; changed/null player irreversibly revokes old outer. If getter exception/Error, invalid genuine value or revoked source, clear the entire old armor snapshot before return/rethrow; always release owner in finally. Keep the existing original slot booleans, slot ordering and native durability validators (item damage/max non-negative, zero maximum still permitted). No inventory mutation, item spoofing, synthetic equipment, GUI changes or Minecraft mapping adjustment.

`Minecraft189PlayerArmorState.update` computes all four durability records as temporary locals BEFORE atomically publishing new mask+durability. This prevents partial writes to already available state even if called directly outside the host.

**18** JUnit Java 8 source-shaped tests with loops cover the lease, all 8 original getter changed/null/same reentries, native getter failures, ItemStack getter reentry, nonfinite or invalid original? (only negative durability is rejected by existing validator), true negative durability rejection, original maximum=0 preserved, transactional state rollback, and genuine recovery. Tests are not original binary gameplay proof.

## Verification
Require exact-head Windows+Ubuntu Foundation and full original Mojang 1.8.9 real graphical X11/dedicated vanilla server 78/78, then independent unchanged-SHA full 78/78. Preserve old failed runs and prior status. All PRs Draft, `main` untouched.
