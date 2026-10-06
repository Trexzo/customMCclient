package dev.trexzo.custommc.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CustomMcBootstrapMainTest {
    private static final List<String> EVENTS =
            new ArrayList<String>();

    @Test
    void invocationSeparatesExplicitTargetFromForwardedArguments() {
        final BootstrapInvocation invocation =
                BootstrapInvocation.parse(
                        new String[]{
                                " example.Target ",
                                "--username",
                                "Player One",
                                "--token",
                                "abc"
                        });

        assertFalse(
                invocation.hasRuntimeInitializer());
        assertEquals(
                "example.Target",
                invocation.targetMainClass());
        assertArrayEquals(
                new String[]{
                        "--username",
                        "Player One",
                        "--token",
                        "abc"
                },
                invocation.targetArguments());
    }

    @Test
    void initializedInvocationSeparatesInitializerTargetAndGameArguments() {
        final BootstrapInvocation invocation =
                BootstrapInvocation.parse(
                        new String[]{
                                BootstrapInvocation.RUNTIME_MARKER,
                                " example.Runtime ",
                                " example.Target ",
                                "--username",
                                "Player One"
                        });

        assertTrue(
                invocation.hasRuntimeInitializer());
        assertEquals(
                "example.Runtime",
                invocation.runtimeInitializerClass());
        assertEquals(
                "example.Target",
                invocation.targetMainClass());
        assertArrayEquals(
                new String[]{
                        "--username",
                        "Player One"
                },
                invocation.targetArguments());
    }

    @Test
    void targetArgumentsAreDefensivelyCopied() {
        final BootstrapInvocation invocation =
                BootstrapInvocation.parse(
                        new String[]{
                                TargetMain.class.getName(),
                                "one"
                        });

        final String[] copy =
                invocation.targetArguments();
        copy[0] = "changed";

        assertArrayEquals(
                new String[]{"one"},
                invocation.targetArguments());
        assertArrayEquals(
                new String[]{"one"},
                invocation.context()
                        .targetArguments());
    }

    @Test
    void bootstrapInvokesTargetMainWithUntouchedRemainingArguments()
            throws Exception {
        TargetMain.lastArguments = null;

        CustomMcBootstrapMain.main(
                new String[]{
                        TargetMain.class.getName(),
                        "alpha",
                        "two words",
                        ""
                });

        assertArrayEquals(
                new String[]{
                        "alpha",
                        "two words",
                        ""
                },
                TargetMain.lastArguments);
    }

    @Test
    void explicitRuntimeSessionWrapsTargetLifetime()
            throws Exception {
        EVENTS.clear();
        TargetMain.lastArguments = null;

        CustomMcBootstrapMain.main(
                new String[]{
                        BootstrapInvocation.RUNTIME_MARKER,
                        RecordingInitializer.class.getName(),
                        TargetMain.class.getName(),
                        "alpha"
                });

        assertEquals(
                Arrays.asList(
                        "initialize:"
                                + TargetMain.class.getName()
                                + ":[alpha]",
                        "target",
                        "close"),
                EVENTS);
        assertArrayEquals(
                new String[]{"alpha"},
                TargetMain.lastArguments);
    }

    @Test
    void targetFailureIsUnwrappedAndSessionStillCloses()
            throws Exception {
        EVENTS.clear();

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> CustomMcBootstrapMain.main(
                                new String[]{
                                        BootstrapInvocation.RUNTIME_MARKER,
                                        RecordingInitializer.class.getName(),
                                        FailingMain.class.getName()
                                }));

        assertEquals(
                "boom",
                failure.getMessage());
        assertEquals(
                Arrays.asList(
                        "initialize:"
                                + FailingMain.class.getName()
                                + ":[]",
                        "target-fail",
                        "close"),
                EVENTS);
    }

    @Test
    void closeFailureIsSuppressedBehindTargetFailure()
            throws Exception {
        EVENTS.clear();

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> CustomMcBootstrapMain.main(
                                new String[]{
                                        BootstrapInvocation.RUNTIME_MARKER,
                                        FailingCloseInitializer.class.getName(),
                                        FailingMain.class.getName()
                                }));

        assertEquals(
                "boom",
                failure.getMessage());
        assertEquals(
                1,
                failure.getSuppressed().length);
        assertEquals(
                "close-boom",
                failure.getSuppressed()[0]
                        .getMessage());
    }

    @Test
    void initializerMustImplementTypedContractAndReturnSession()
            throws Exception {
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomMcBootstrapMain.main(
                        new String[]{
                                BootstrapInvocation.RUNTIME_MARKER,
                                NoMain.class.getName(),
                                TargetMain.class.getName()
                        }));

        assertThrows(
                NullPointerException.class,
                () -> CustomMcBootstrapMain.main(
                        new String[]{
                                BootstrapInvocation.RUNTIME_MARKER,
                                NullSessionInitializer.class.getName(),
                                TargetMain.class.getName()
                        }));
    }

    @Test
    void invalidTargetContractIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BootstrapInvocation.parse(
                        new String[0]));
        assertThrows(
                IllegalArgumentException.class,
                () -> BootstrapInvocation.parse(
                        new String[]{" "}));
        assertThrows(
                IllegalArgumentException.class,
                () -> BootstrapInvocation.parse(
                        new String[]{
                                BootstrapInvocation.RUNTIME_MARKER,
                                RecordingInitializer.class.getName()
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> BootstrapInvocation.parse(
                        new String[]{
                                CustomMcBootstrapMain.class.getName()
                        }));
        assertThrows(
                NoSuchMethodException.class,
                () -> CustomMcBootstrapMain.main(
                        new String[]{
                                NoMain.class.getName()
                        }));
    }

    public static final class TargetMain {
        private static String[] lastArguments;

        public static void main(
                final String[] arguments) {
            EVENTS.add("target");
            lastArguments =
                    Arrays.copyOf(
                            arguments,
                            arguments.length);
        }
    }

    public static final class FailingMain {
        public static void main(
                final String[] arguments) {
            EVENTS.add("target-fail");
            throw new IllegalStateException(
                    "boom");
        }
    }

    public static final class RecordingInitializer
            implements BootstrapRuntimeInitializer {
        public RecordingInitializer() {
        }

        @Override
        public BootstrapRuntimeSession initialize(
                final BootstrapContext context) {
            EVENTS.add(
                    "initialize:"
                            + context.targetMainClass()
                            + ":"
                            + Arrays.toString(
                            context.targetArguments()));
            return new BootstrapRuntimeSession() {
                @Override
                public void close() {
                    EVENTS.add("close");
                }
            };
        }
    }

    public static final class FailingCloseInitializer
            implements BootstrapRuntimeInitializer {
        public FailingCloseInitializer() {
        }

        @Override
        public BootstrapRuntimeSession initialize(
                final BootstrapContext context) {
            return new BootstrapRuntimeSession() {
                @Override
                public void close() {
                    throw new IllegalStateException(
                            "close-boom");
                }
            };
        }
    }

    public static final class NullSessionInitializer
            implements BootstrapRuntimeInitializer {
        public NullSessionInitializer() {
        }

        @Override
        public BootstrapRuntimeSession initialize(
                final BootstrapContext context) {
            return null;
        }
    }

    public static final class NoMain {
        public NoMain() {
        }
    }
}
