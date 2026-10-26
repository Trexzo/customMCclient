package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Opt-in official-client-only melee acceptance: a real vanilla clickMouse
 * must start on an actual raycast entity with zero baseline hurtTime, and
 * that very same loaded entity must subsequently acquire native hurtTime.
 * Never enables a Combat module, edits health, or synthesizes damage.
 */
final class Minecraft189VanillaMeleeAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.reportVanillaMelee";
    private static Object observedWorld;
    private static int[] observedCombat;
    private static Object pendingWorld;
    private static Object pendingEntity;
    private static long pendingTick;
    private static long ticks;
    private static boolean reported;
    private static boolean syntheticCall;
    private static boolean pendingAutoClicker;
    private static boolean syntheticTrigger;
    private static boolean pendingTrigger;
    private static boolean triggerControllerReached;
    private static boolean syntheticAura;
    private static boolean pendingAura;
    private static boolean auraRaycastLogged;
    private static boolean auraControllerReached;
    private static boolean auraPositiveCounterReported;
    private static boolean auraReadyCounterReported;
    // V327 opt-in diagnostic only: never grants a click or an acceptance.
    private static boolean autoclickerBlockedCounterReported;
    private static boolean autoclickerReadyCounterReported;
    // V329 opt-in bounded provenance: differentiate missing native hook,
    // unsatisfied test property, and lost synthetic-click ownership.
    private static boolean autoclickerSyntheticEntryProvenanceReported;
    private static boolean autoclickerFirstGuardCallReported;
    private static boolean autoclickerSyntheticGuardCallReported;
    private static final Minecraft189OriginalNonPlayerRaycastTrace
            nonPlayerRaycastTrace =
            new Minecraft189OriginalNonPlayerRaycastTrace();

    private Minecraft189VanillaMeleeAcceptance() {
    }

    static void reset() {
        observedWorld = null;
        observedCombat = null;
        pendingWorld = null;
        pendingEntity = null;
        pendingTick = 0;
        ticks = 0;
        reported = false;
        syntheticCall = false;
        pendingAutoClicker = false;
        syntheticTrigger = false;
        pendingTrigger = false;
        triggerControllerReached = false;
        syntheticAura = false;
        pendingAura = false;
        auraRaycastLogged = false;
        auraControllerReached = false;
        auraPositiveCounterReported = false;
        auraReadyCounterReported = false;
        autoclickerBlockedCounterReported = false;
        autoclickerReadyCounterReported = false;
        autoclickerSyntheticEntryProvenanceReported = false;
        autoclickerFirstGuardCallReported = false;
        autoclickerSyntheticGuardCallReported = false;
        nonPlayerRaycastTrace.reset();
    }

    static void tick() {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }
        ticks++;
        if (pendingEntity != null && ticks - pendingTick > 20) {
            pendingEntity = null;
            pendingWorld = null;
        }
    }

    static void onNativeClick(final Object minecraft) {
        if (syntheticCall
                && Boolean.getBoolean("custommc.acceptance.reportAutoClicker")
                && !autoclickerSyntheticEntryProvenanceReported) {
            autoclickerSyntheticEntryProvenanceReported = true;
            System.out.println("CUSTOMMC_189_V329_SYNTHETIC_CLICK_ENTRY"
                    + " exactRemoteProperty=" + Boolean.getBoolean(
                            "custommc.acceptance.requireAutoClickerExactRemoteUuid")
                    + " vanillaMeleeProperty=" + Boolean.getBoolean(PROPERTY)
                    + " nativeInputPresent=" + (minecraft != null));
            System.out.flush();
        }
        if (!Boolean.getBoolean(PROPERTY) || reported
                || minecraft == null || observedCombat == null) {
            return;
        }
        try {
            // Proven in the official 1.8.9 mappings:
            // ave.s = objectMouseOver, auh.a = typeOfHit,
            // auh$a.c = ENTITY, auh.d = entityHit, ave.f = world.
            final Object hit = field(minecraft, "s");
            if (hit == null) {
                return;
            }
            final ClassLoader gameLoader = minecraft.getClass().getClassLoader();
            final Class<?> hitType = Class.forName("auh$a", false, gameLoader);
            if (field(hit, "a") != field(hitType, null, "c")) {
                return;
            }
            final Object entity = field(hit, "d");
            final Object world = field(minecraft, "f");
            if (entity == null || world == null || world != observedWorld
                    || entity == field(minecraft, "h")) {
                return;
            }
            final List<?> loaded = loadedEntities(world);
            final int index = identityIndex(loaded, entity);
            if (index < 0 || index >= observedCombat.length) {
                return;
            }
            final int baseline = observedCombat[index];
            if (baseline < 0 || (baseline & 1) == 0
                    || ((baseline & 255) >>> 1) != 0) {
                return;
            }
            // Do not reset the 20-tick window on every high-CPS Aura
            // attempt against the same unhurt real player, or flood logs.
            if (syntheticAura && pendingAura
                    && pendingWorld == world && pendingEntity == entity
                    && ticks >= pendingTick && ticks - pendingTick <= 20) {
                return;
            }
            pendingWorld = world;
            pendingEntity = entity;
            pendingTick = ticks;
            pendingAura = syntheticAura
                    && Boolean.getBoolean(
                            "custommc.acceptance.reportKillAuraLocalPvp");
            if (pendingAura) {
                // A real second player must be present in the game's native
                // entity-kind list. A pig or client-only fake cannot certify
                // this target identity from a server-owned player join.
                if (!(world instanceof Minecraft189WorldEntityKindsAccess)) {
                    pendingAura = false;
                    pendingEntity = null;
                    return;
                }
                final int[] kinds = ((Minecraft189WorldEntityKindsAccess) world)
                        .customMcLoadedEntityKinds();
                if (kinds == null || index >= kinds.length
                        || (kinds[index]
                                & (Minecraft189WorldEntityKindState.PLAYER
                                   | Minecraft189WorldEntityKindState.LIVING))
                            != (Minecraft189WorldEntityKindState.PLAYER
                                | Minecraft189WorldEntityKindState.LIVING)
                        || (kinds[index]
                                & Minecraft189WorldEntityKindState.LOCAL_PLAYER)
                            != 0) {
                    pendingAura = false;
                    pendingEntity = null;
                    return;
                }
                if (!auraRaycastLogged) {
                    auraRaycastLogged = true;
                    System.out.println(
                            "CUSTOMMC_189_KILLAURA_REAL_PLAYER_RAYCAST_ARMED=YES");
                    System.out.flush();
                }
            }
            pendingTrigger = syntheticTrigger
                    && Boolean.getBoolean(
                            "custommc.acceptance.reportTriggerBotRealPvp");
            if (pendingTrigger) {
                // The exact original World loaded entity must be a REMOTE
                // genuine player, never an arbitrary native living mob.
                if (!(world instanceof Minecraft189WorldEntityKindsAccess)) {
                    pendingTrigger = false;
                    pendingEntity = null;
                    return;
                }
                final int[] nativeKinds =
                        ((Minecraft189WorldEntityKindsAccess) world)
                                .customMcLoadedEntityKinds();
                if (nativeKinds == null || index >= nativeKinds.length
                        || (nativeKinds[index]
                                & (Minecraft189WorldEntityKindState.PLAYER
                                   | Minecraft189WorldEntityKindState.LIVING))
                            != (Minecraft189WorldEntityKindState.PLAYER
                                | Minecraft189WorldEntityKindState.LIVING)
                        || (nativeKinds[index]
                                & Minecraft189WorldEntityKindState.LOCAL_PLAYER) != 0) {
                    pendingTrigger = false;
                    pendingEntity = null;
                    return;
                }
                System.out.println(
                        "CUSTOMMC_189_TRIGGERBOT_ORIGINAL_PLAYER_RAYCAST_ARMED=YES");
                System.out.flush();
            }
            pendingAutoClicker = syntheticCall
                    && Boolean.getBoolean("custommc.acceptance.reportAutoClicker");
            if (pendingAutoClicker) {
                System.out.println("CUSTOMMC_189_AUTOCLICKER_REAL_TARGET_ARMED=YES");
                System.out.flush();
            }
            System.out.println("CUSTOMMC_189_VANILLA_MELEE_TARGET_ARMED=YES");
            System.out.flush();
        } catch (ReflectiveOperationException | SecurityException failure) {
            // An invalid exact game mapping cannot ever certify a hit.
            // Keep normal vanilla clickMouse behavior unchanged.
        }
    }

    static void onNativeClickGuard(final int nativeCounter) {
        // V329: report call-site reachability even when the separate exact
        // remote-property prerequisite is absent. Both outcomes are bounded
        // by reset and never certify original attack/controller/server damage.
        if (Boolean.getBoolean("custommc.acceptance.reportAutoClicker")) {
            if (!autoclickerFirstGuardCallReported) {
                autoclickerFirstGuardCallReported = true;
                System.out.println("CUSTOMMC_189_V329_FIRST_GUARD_CALLBACK"
                        + " synthetic=" + syntheticCall
                        + " exactRemoteProperty=" + Boolean.getBoolean(
                                "custommc.acceptance.requireAutoClickerExactRemoteUuid")
                        + " sourceCounter=" + nativeCounter);
                System.out.flush();
            }
            if (syntheticCall && !autoclickerSyntheticGuardCallReported) {
                autoclickerSyntheticGuardCallReported = true;
                System.out.println("CUSTOMMC_189_V329_SYNTHETIC_GUARD_CALLBACK"
                        + " exactRemoteProperty=" + Boolean.getBoolean(
                                "custommc.acceptance.requireAutoClickerExactRemoteUuid")
                        + " sourceCounter=" + nativeCounter);
                System.out.flush();
            }
        }
        // Observational V327 native Mojang clickMouse guard evidence.
        // V326's original remote-player attack must still reach the real
        // controller and damage the server player to pass. Logging can
        // neither clear leftClickCounter nor supply a success marker.
        if (syntheticCall && Boolean.getBoolean(
                "custommc.acceptance.requireAutoClickerExactRemoteUuid")) {
            if (nativeCounter > 0 && !autoclickerBlockedCounterReported) {
                autoclickerBlockedCounterReported = true;
                System.out.println(
                        "CUSTOMMC_189_AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_BLOCKED="
                                + nativeCounter);
                System.out.flush();
            } else if (nativeCounter <= 0
                    && !autoclickerReadyCounterReported) {
                autoclickerReadyCounterReported = true;
                System.out.println(
                        "CUSTOMMC_189_AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_READY=YES");
                System.out.flush();
            }
        }
        if (!Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraLocalPvp")
                || !syntheticAura) return;
        if (nativeCounter > 0 && !auraPositiveCounterReported) {
            auraPositiveCounterReported = true;
            System.out.println(
                    "CUSTOMMC_189_MOJANG_LEFT_CLICK_COUNTER_BLOCKED="
                            + nativeCounter);
            System.out.flush();
        } else if (nativeCounter <= 0 && !auraReadyCounterReported) {
            auraReadyCounterReported = true;
            System.out.println(
                    "CUSTOMMC_189_MOJANG_LEFT_CLICK_COUNTER_READY=YES");
            System.out.flush();
        }
    }

    static void onMojangControllerAttack(final Object controller,
            final Object player, final Object target) {
        if (Boolean.getBoolean("custommc.acceptance.reportTriggerBotRealPvp")
                && !triggerControllerReached && syntheticTrigger
                && pendingTrigger && pendingEntity != null
                && target == pendingEntity && player != null && controller != null) {
            triggerControllerReached = true;
            System.out.println(
                    "CUSTOMMC_189_TRIGGERBOT_REAL_MOJANG_CONTROLLER_ARGUMENT_PASS=YES");
            System.out.flush();
        }
        if (!Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraLocalPvp")
                || auraControllerReached || !syntheticAura
                || !pendingAura || pendingEntity == null
                || target != pendingEntity || player == null
                || controller == null) {
            return;
        }
        auraControllerReached = true;
        System.out.println(
                "CUSTOMMC_189_MOJANG_PLAYER_CONTROLLER_ATTACK_ENTITY_ENTERED=YES");
        System.out.flush();
    }

    static void onWorldCombat(final Minecraft189WorldEntityCombatAccess world) {
        if (!Boolean.getBoolean(PROPERTY) || world == null || reported) {
            return;
        }
        try {
            final int[] states = world.customMcLoadedEntityCombatStates();
            if (states == null) {
                observedWorld = null;
                observedCombat = null;
                return;
            }
            final Object currentWorld = world;
            observedWorld = currentWorld;
            observedCombat = Arrays.copyOf(states, states.length);
            if (pendingEntity == null || pendingWorld != currentWorld
                    || ticks - pendingTick > 20
                    || ticks < pendingTick) {
                return;
            }
            final List<?> loaded = loadedEntities(currentWorld);
            final int index = identityIndex(loaded, pendingEntity);
            if (index < 0 || index >= states.length) {
                return;
            }
            final int current = states[index];
            if (current >= 0 && (current & 1) != 0
                    && ((current & 255) >>> 1) > 0) {
                reported = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_REAL_ENTITY_HURT_AFTER_VANILLA_CLICK_PASS=YES");
                if (pendingAutoClicker) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_AUTOCLICKER_SAME_ENTITY_HURT_PASS=YES");
                }
                if (pendingTrigger && triggerControllerReached) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_TRIGGERBOT_REAL_PLAYER_HURT_PASS=YES");
                }
                if (pendingAura) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_KILLAURA_PLAYER_HURT_PASS=YES");
                }
                System.out.flush();
            }
        } catch (ReflectiveOperationException | SecurityException failure) {
            observedWorld = null;
            observedCombat = null;
        }
    }

    /**
     * Requires genuine current native raycast entity and a fresh zero-hurt
     * combat snapshot. Called at the real Minecraft auto-click callback, not
     * from synthetic test fixtures or vanilla input event impersonation.
     */
    static boolean cleanCrosshairEntity(final Object minecraft) {
        // Shared read-only original raycast/hurt-time witness. Each named
        // acceptance must opt in independently: V72 deliberately does NOT
        // enable Auto Clicker (TriggerBot isolation is a required negative).
        if ((!Boolean.getBoolean("custommc.acceptance.reportAutoClicker")
                && !Boolean.getBoolean("custommc.acceptance.reportTriggerBotRealPvp"))
                || minecraft == null || observedCombat == null) {
            return false;
        }
        try {
            final Object hit = field(minecraft, "s");
            if (hit == null) return false;
            final ClassLoader loader = minecraft.getClass().getClassLoader();
            final Class<?> type = Class.forName("auh$a", false, loader);
            if (field(hit, "a") != field(type, null, "c")) return false;
            final Object entity = field(hit, "d");
            final Object world = field(minecraft, "f");
            if (world == null || entity == null || world != observedWorld
                    || entity == field(minecraft, "h")) return false;
            final int index = identityIndex(loadedEntities(world), entity);
            if (index < 0 || index >= observedCombat.length) return false;
            final int state = observedCombat[index];
            return state >= 0 && (state & 1) == 1
                    && ((state & 255) >>> 1) == 0;
        } catch (ReflectiveOperationException | SecurityException invalid) {
            return false;
        }
    }

    /**
     * Exact native 1.8.9 raycast + loaded-world identity verification for the
     * CI Kill Aura *nonplayer* veto. This observes a real living nonplayer;
     * it does not create or reclassify entities.
     */
    static Minecraft189OriginalNonPlayerRaycastTrace.Snapshot
            originalNonPlayerRaycastDiagnostic() {
        return nonPlayerRaycastTrace.snapshot();
    }

    private static Object nonPlayerRejected(final String reason,
            final int nativeIndex, final int kindBits,
            final int nativeEntities, final int kindsCount) {
        nonPlayerRaycastTrace.observe(reason, nativeIndex, kindBits,
                nativeEntities, kindsCount, false);
        return null;
    }

    static Object raycastLivingNonPlayer(
            final Object minecraft, final int[] kinds) {
        if (!Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraNonPlayer"))
            return null;
        final int kindsCount = kinds == null ? -1 : kinds.length;
        if (minecraft == null || kinds == null)
            return nonPlayerRejected("native-input-or-kind-source-unavailable",
                    -1, -1, -1, kindsCount);
        if (observedWorld == null)
            return nonPlayerRejected("original-world-source-not-observed",
                    -1, -1, -1, kindsCount);
        try {
            final Object hit = field(minecraft, "s");
            if (hit == null)
                return nonPlayerRejected("original-crosshair-no-ray",
                        -1, -1, -1, kindsCount);
            final ClassLoader gameLoader = minecraft.getClass().getClassLoader();
            final Class<?> type = Class.forName("auh$a", false, gameLoader);
            if (field(hit, "a") != field(type, null, "c"))
                return nonPlayerRejected("original-crosshair-not-entity-hit",
                        -1, -1, -1, kindsCount);
            final Object entity = field(hit, "d");
            final Object world = field(minecraft, "f");
            if (entity == null)
                return nonPlayerRejected("original-ray-entity-null",
                        -1, -1, -1, kindsCount);
            if (world == null)
                return nonPlayerRejected("original-world-null",
                        -1, -1, -1, kindsCount);
            if (world != observedWorld)
                return nonPlayerRejected("original-world-identity-changed",
                        -1, -1, -1, kindsCount);
            if (entity == field(minecraft, "h"))
                return nonPlayerRejected("original-ray-is-local-player",
                        -1, -1, -1, kindsCount);
            // EXACTLY the same original loadedEntityList reflection read,
            // identity lookup and kind verification as before this patch.
            final List<?> loaded = loadedEntities(world);
            final int index = identityIndex(loaded, entity);
            if (index < 0)
                return nonPlayerRejected("original-ray-not-in-loaded-entities",
                        index, -1, loaded.size(), kindsCount);
            if (index >= kinds.length)
                return nonPlayerRejected("original-ray-index-exceeds-kinds",
                        index, -1, loaded.size(), kindsCount);
            final int flags = kinds[index];
            if ((flags & Minecraft189WorldEntityKindState.LIVING) == 0
                    || (flags & Minecraft189WorldEntityKindState.PLAYER) != 0
                    || (flags & Minecraft189WorldEntityKindState.LOCAL_PLAYER) != 0) {
                return nonPlayerRejected("original-kind-not-living-nonplayer",
                        index, flags, loaded.size(), kindsCount);
            }
            nonPlayerRaycastTrace.observe(
                    "original-living-nonplayer-ray-accepted",
                    index, flags, loaded.size(), kindsCount, true);
            return entity;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return nonPlayerRejected("original-ray-reflection-unavailable",
                    -1, -1, -1, kindsCount);
        }
    }

    static void syntheticTriggerStarted() {
        syntheticTrigger = true;
    }

    static void syntheticTriggerFinished() {
        syntheticTrigger = false;
    }

    static void syntheticAuraStarted() {
        syntheticAura = true;
    }

    static void syntheticAuraFinished() {
        syntheticAura = false;
    }

    static void syntheticClickStarted() {
        syntheticCall = true;
    }

    static void syntheticClickFinished() {
        syntheticCall = false;
    }

    private static List<?> loadedEntities(final Object world)
            throws ReflectiveOperationException {
        // adm.f = loadedEntityList; access it through the runtime class chain.
        final Object value = field(world, "f");
        if (!(value instanceof List<?>)) {
            throw new IllegalStateException("mapped loadedEntityList is not a List");
        }
        return (List<?>) value;
    }

    private static int identityIndex(final List<?> loaded, final Object entity) {
        for (int i = 0; i < loaded.size(); i++) {
            if (loaded.get(i) == entity) {
                return i;
            }
        }
        return -1;
    }

    private static Object field(final Object target, final String name)
            throws ReflectiveOperationException {
        return field(target.getClass(), target, name);
    }

    private static Object field(final Class<?> owner, final Object target,
            final String name) throws ReflectiveOperationException {
        for (Class<?> cls = owner; cls != null; cls = cls.getSuperclass()) {
            try {
                final Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException absent) {
                // Some exact mapped fields are declared on a vanilla parent.
            }
        }
        throw new NoSuchFieldException(owner.getName() + "." + name);
    }
}
