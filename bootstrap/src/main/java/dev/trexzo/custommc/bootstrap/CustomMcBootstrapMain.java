package dev.trexzo.custommc.bootstrap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public final class CustomMcBootstrapMain {
    private CustomMcBootstrapMain() {
    }

    public static void main(
            final String[] arguments)
            throws Exception {
        final BootstrapInvocation invocation =
                BootstrapInvocation.parse(
                        arguments);
        final ClassLoader loader =
                Thread.currentThread()
                        .getContextClassLoader();

        BootstrapRuntimeSession session = null;
        Throwable failure = null;
        try {
            if (invocation.hasRuntimeInitializer()) {
                session =
                        initializeRuntime(
                                invocation,
                                loader);
            }

            invokeTarget(
                    invocation,
                    loader);
        } catch (Throwable throwable) {
            failure = throwable;
            rethrow(throwable);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Throwable closeFailure) {
                    if (failure != null) {
                        failure.addSuppressed(
                                closeFailure);
                    } else if (closeFailure instanceof Exception) {
                        throw (Exception) closeFailure;
                    } else if (closeFailure instanceof Error) {
                        throw (Error) closeFailure;
                    } else {
                        throw new RuntimeException(
                                closeFailure);
                    }
                }
            }
        }
    }

    private static void rethrow(
            final Throwable failure)
            throws Exception {
        if (failure instanceof Exception) {
            throw (Exception) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        throw new RuntimeException(failure);
    }

    private static BootstrapRuntimeSession initializeRuntime(
            final BootstrapInvocation invocation,
            final ClassLoader loader)
            throws Exception {
        final Class<?> initializerClass =
                Class.forName(
                        invocation.runtimeInitializerClass(),
                        true,
                        loader);

        if (!BootstrapRuntimeInitializer.class
                .isAssignableFrom(
                        initializerClass)) {
            throw new IllegalArgumentException(
                    "runtime initializer must implement "
                            + BootstrapRuntimeInitializer.class
                            .getName());
        }

        final Constructor<?> constructor =
                initializerClass.getDeclaredConstructor();
        if (!Modifier.isPublic(
                constructor.getModifiers())) {
            throw new IllegalArgumentException(
                    "runtime initializer constructor must be public");
        }

        final BootstrapRuntimeInitializer initializer =
                (BootstrapRuntimeInitializer)
                        constructor.newInstance();
        return Objects.requireNonNull(
                initializer.initialize(
                        invocation.context()),
                "runtime initializer session");
    }

    private static void invokeTarget(
            final BootstrapInvocation invocation,
            final ClassLoader loader)
            throws Exception {
        final Class<?> target =
                Class.forName(
                        invocation.targetMainClass(),
                        true,
                        loader);
        final Method main =
                target.getMethod(
                        "main",
                        String[].class);

        if (!Modifier.isStatic(
                main.getModifiers())
                || main.getReturnType()
                != Void.TYPE) {
            throw new IllegalArgumentException(
                    "target main must be static void main(String[])");
        }

        try {
            main.invoke(
                    null,
                    (Object) invocation.targetArguments());
        } catch (InvocationTargetException failure) {
            final Throwable cause =
                    failure.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw failure;
        }
    }
}
