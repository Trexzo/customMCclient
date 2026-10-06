package dev.trexzo.custommc.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
