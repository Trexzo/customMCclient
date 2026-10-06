package dev.trexzo.custommc.bootstrap;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TransformingTargetClassLoaderTest {
    @Test
    void claimedClassIsDefinedFromTransformedBytesByChildLoader()
            throws Exception {
        final ReplacingTransformer transformer =
                new ReplacingTransformer(
                        TransformTarget.class.getName());
        final TransformingTargetClassLoader loader =
                new TransformingTargetClassLoader(
                        testClassPath(),
                        getClass().getClassLoader(),
                        transformer);

        try {
            final Class<?> transformed =
                    loader.loadClass(
                            TransformTarget.class.getName());

            assertNotSame(
                    TransformTarget.class,
                    transformed);
            assertSame(
                    loader,
                    transformed.getClassLoader());
            assertEquals(
                    1,
                    transformer.calls);

            final Method value =
                    transformed.getMethod(
                            "value");
            assertEquals(
                    "mutated!",
                    value.invoke(null));

            assertSame(
                    transformed,
                    loader.loadClass(
                            TransformTarget.class.getName()));
            assertEquals(
                    1,
                    transformer.calls);
        } finally {
            loader.close();
        }
    }

    @Test
    void unclaimedClassesPreserveParentIdentity()
            throws Exception {
        final TransformingTargetClassLoader loader =
                new TransformingTargetClassLoader(
                        testClassPath(),
                        getClass().getClassLoader(),
                        new NeverTransformer());

        try {
            assertSame(
                    CustomMcBootstrapMain.class,
                    loader.loadClass(
                            CustomMcBootstrapMain.class
                                    .getName()));
            assertSame(
                    String.class,
                    loader.loadClass(
                            String.class.getName()));
        } finally {
            loader.close();
        }
    }

    @Test
    void claimedMissingClassCannotSilentlyBypassTransformation()
            throws Exception {
        final TransformingTargetClassLoader loader =
                new TransformingTargetClassLoader(
                        testClassPath(),
                        getClass().getClassLoader(),
                        new MissingTransformer());

        try {
            assertThrows(
                    ClassNotFoundException.class,
                    () -> loader.loadClass(
                            "example.claimed.Missing"));
        } finally {
            loader.close();
        }
    }

    private static URL[] testClassPath() {
        final URL location =
                TransformingTargetClassLoaderTest.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation();
        return new URL[]{location};
    }

    public static final class TransformTarget {
        private TransformTarget() {
        }

        public static String value() {
            return "original";
        }
    }

    private static final class ReplacingTransformer
            implements BootstrapClassTransformer {
        private static final byte[] SOURCE =
                "original".getBytes(
                        StandardCharsets.UTF_8);
        private static final byte[] REPLACEMENT =
                "mutated!".getBytes(
                        StandardCharsets.UTF_8);

        private final String target;
        private int calls;

        ReplacingTransformer(
                final String target) {
            this.target = target;
        }

        @Override
        public boolean handles(
                final String binaryClassName) {
            return target.equals(
                    binaryClassName);
        }

        @Override
        public byte[] transform(
                final String binaryClassName,
                final byte[] originalBytes) {
            calls++;
            final byte[] transformed =
                    Arrays.copyOf(
                            originalBytes,
                            originalBytes.length);

            final int offset =
                    indexOf(
                            transformed,
                            SOURCE);
            if (offset < 0) {
                throw new IllegalStateException(
                        "source constant not found");
            }

            System.arraycopy(
                    REPLACEMENT,
                    0,
                    transformed,
                    offset,
                    REPLACEMENT.length);
            return transformed;
        }

        private static int indexOf(
                final byte[] haystack,
                final byte[] needle) {
            for (int start = 0;
                 start <= haystack.length
                         - needle.length;
                 start++) {
                boolean match = true;
                for (int index = 0;
                     index < needle.length;
                     index++) {
                    if (haystack[start + index]
                            != needle[index]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return start;
                }
            }
            return -1;
        }
    }

    private static final class NeverTransformer
            implements BootstrapClassTransformer {
        @Override
        public boolean handles(
                final String binaryClassName) {
            return false;
        }

        @Override
        public byte[] transform(
                final String binaryClassName,
                final byte[] originalBytes) {
            throw new AssertionError(
                    "unclaimed class must not transform");
        }
    }

    private static final class MissingTransformer
            implements BootstrapClassTransformer {
        @Override
        public boolean handles(
                final String binaryClassName) {
            return binaryClassName.startsWith(
                    "example.claimed.");
        }

        @Override
        public byte[] transform(
                final String binaryClassName,
                final byte[] originalBytes) {
            throw new AssertionError(
                    "missing class must not reach transformer");
        }
    }
}
