# V267 — original Minecraft world time/weather native source ownership

Parent V266 Draft PR #737, exact SHA `0f1d8e6419ac8b4dddb55b46ea4bf9893e61a7e7`.

## Concrete source audit
Original `Minecraft189HostRuntime.worldTime` read `world.customMcWorldTime()` without original World source ownership, and its exception path could preserve an old HUD clock. Original `worldWeather` read `world.customMcRaining()` and `world.customMcThundering()` from two separate genuine mapped callbacks without any source revocation check or clear-on-throw. An original getter may synchronously call the host with another/null World before returning, so outer callback could publish a mixed or unloaded World weather snapshot. That is a true stale-source path; this is not an observed native CI failure at the current snapshot.

## Bounded implementation
Add two typed `Minecraft189ExactSourceLease` owners for `Minecraft189WorldTimeAccess` and `Minecraft189WorldWeatherAccess`. Run the mapped time getter exactly once, validate its original reference source before updating the unchanged long-time state. Run mapped rain getter and mapped thunder getter exactly once each, requiring the **same original World** after both callbacks before publishing the unchanged rain/thunder state. Nested same-World callback never consumes duplicate getter; nested different/null World irreversibly revokes outer source. Getter RuntimeException or Error clears previous HUD state and propagates untouched, finally releasing lease. Null World explicitly clears existing state. Two states/leases remain separate: a world-time getter may legitimately synchronously populate weather without mixing values.

The unchanged original Weather HUD still supports clear sky, rainfall and thunder; unchanged World time supports any original long tick value. No World writes, synthetic weather effects, time changes, Minecraft mappings, player movement, GUI, gameplay flags, packet mutations or acceptance bypasses.

20 deterministic JUnit Java 8 source tests prove real-accessor invocation shape, both weather getter callback reentry stages, same/different/null Worlds, exceptions/Errors, world unload, previous state invalidation, independent time and weather, long world ticks, and recovery. This source-level test suite does not establish native Minecraft proof.

**Nonnegotiable certification:** exact-head Windows+Ubuntu Foundation 10/10, complete original pinned Mojang 1.8.9 X11 GUI+vanilla dedicated server 78/78, then independent unchanged commit SHA full 78/78. Preserve every earlier failed original run. All PRs remain Draft; `main` unchanged.
