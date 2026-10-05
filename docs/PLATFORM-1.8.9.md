# Minecraft 1.8.9 platform seam

M8 introduces the first dedicated game-version module:

`platform-1.8.9`

It intentionally contains no Mojang/Minecraft source and has no compile-time dependency on Minecraft classes.

## Purpose

The platform module translates future 1.8.9 host hooks into stable project-level events.

The initial bridge surface is deliberately small:

- client tick start/end;
- render frame with partial-tick value;
- world presence changes.

These are published as version-independent platform events through the core `EventBus`.

## Lifecycle

`Minecraft189Platform.attach()` creates one event bridge and registers it as the active `GameEventBridge` service.

`detach()`:

1. deactivates the bridge;
2. removes the service registration;
3. invalidates retained bridge references.

The service registry now returns explicit registration handles so platform-owned services cannot remain globally registered after detach.

## Tick correctness

Tick start/end pairing is enforced. A duplicate start or an end without a start is rejected instead of silently corrupting sequence ownership.

A completed start/end pair shares one sequence number. The sequence increments only after the end event.

## World event deduplication

World-presence events publish only when the state changes, avoiding duplicate transition events.

## Next integration layer

A later milestone will implement the host hook/bootstrap layer that calls this bridge from the legally acquired 1.8.9 runtime. That hook layer remains isolated from `core` and `platform-api`.
