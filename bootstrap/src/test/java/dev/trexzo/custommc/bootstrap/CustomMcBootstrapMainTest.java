package dev.trexzo.custommc.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CustomMcBootstrapMainTest {
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
    void targetFailureIsUnwrapped()
            throws Exception {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> CustomMcBootstrapMain.main(
                                new String[]{
                                        FailingMain.class.getName()
                                }));

        assertEquals(
                "boom",
                failure.getMessage());
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
            lastArguments =
                    Arrays.copyOf(
                            arguments,
                            arguments.length);
        }
    }

    public static final class FailingMain {
        public static void main(
                final String[] arguments) {
            throw new IllegalStateException(
                    "boom");
        }
    }

    public static final class NoMain {
        private NoMain() {
        }
    }
}
