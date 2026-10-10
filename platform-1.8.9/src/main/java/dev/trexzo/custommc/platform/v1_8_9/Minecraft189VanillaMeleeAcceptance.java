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
            pendingWorld = world;
            pendingEntity = entity;
            pendingTick = ticks;
            System.out.println("CUSTOMMC_189_VANILLA_MELEE_TARGET_ARMED=YES");
            System.out.flush();
        } catch (ReflectiveOperationException | SecurityException failure) {
            // An invalid exact game mapping cannot ever certify a hit.
            // Keep normal vanilla clickMouse behavior unchanged.
        }
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
                System.out.flush();
            }
        } catch (ReflectiveOperationException | SecurityException failure) {
            observedWorld = null;
            observedCombat = null;
        }
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
