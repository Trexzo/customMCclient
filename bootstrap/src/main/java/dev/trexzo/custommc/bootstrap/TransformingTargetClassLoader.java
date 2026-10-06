package dev.trexzo.custommc.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class TransformingTargetClassLoader
        extends URLClassLoader {
    private static final int BUFFER_SIZE = 8192;

    private final BootstrapClassTransformer transformer;

    public TransformingTargetClassLoader(
            final URL[] classPath,
            final ClassLoader parent,
            final BootstrapClassTransformer transformer) {
        super(
                copyClassPath(classPath),
                parent);
        this.transformer =
                Objects.requireNonNull(
                        transformer,
                        "transformer");
    }

    public static TransformingTargetClassLoader fromJavaClassPath(
            final ClassLoader parent,
            final BootstrapClassTransformer transformer) {
        return new TransformingTargetClassLoader(
                currentJavaClassPath(),
                parent,
                transformer);
    }

    @Override
    protected Class<?> loadClass(
            final String name,
            final boolean resolve)
            throws ClassNotFoundException {
        Objects.requireNonNull(
                name,
                "name");

        if (!transformer.handles(name)) {
            return super.loadClass(
                    name,
                    resolve);
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded =
                    findLoadedClass(name);
            if (loaded == null) {
                loaded =
                        findTransformedClass(
                                name);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    private Class<?> findTransformedClass(
            final String binaryName)
            throws ClassNotFoundException {
        final String resourceName =
                binaryName.replace(
                        '.',
                        '/')
                        + ".class";
        final URL resource =
                findResource(
                        resourceName);
        if (resource == null) {
            throw new ClassNotFoundException(
                    "claimed transform class is absent from target classpath: "
                            + binaryName);
        }

        final byte[] original;
        try {
            original =
                    readAll(resource);
        } catch (IOException failure) {
            throw new ClassNotFoundException(
                    "failed reading transform class: "
                            + binaryName,
                    failure);
        }

        final byte[] transformed;
        try {
            transformed =
                    Objects.requireNonNull(
                            transformer.transform(
                                    binaryName,
                                    original),
                            "transformed class bytes");
        } catch (Exception failure) {
            throw new ClassNotFoundException(
                    "failed transforming class: "
                            + binaryName,
                    failure);
        }

        if (transformed.length == 0) {
            throw new ClassNotFoundException(
                    "transformer returned empty class bytes: "
                            + binaryName);
        }

        return defineClass(
                binaryName,
                transformed,
                0,
                transformed.length);
    }

    private static byte[] readAll(
            final URL resource)
            throws IOException {
        try (InputStream input =
                     resource.openStream();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {
            final byte[] buffer =
                    new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(
                        buffer,
                        0,
                        read);
            }
            return output.toByteArray();
        }
    }

    private static URL[] currentJavaClassPath() {
        final String raw =
                System.getProperty(
                        "java.class.path");
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalStateException(
                    "java.class.path is empty");
        }

        final String[] entries =
                raw.split(
                        Pattern.quote(
                                File.pathSeparator),
                        -1);
        final List<URL> urls =
                new ArrayList<URL>(
                        entries.length);
        for (String entry : entries) {
            final File file =
                    new File(
                            entry.isEmpty()
                                    ? "."
                                    : entry);
            try {
                urls.add(
                        file.getAbsoluteFile()
                                .toURI()
                                .toURL());
            } catch (MalformedURLException failure) {
                throw new IllegalStateException(
                        "invalid classpath entry: "
                                + entry,
                        failure);
            }
        }
        return urls.toArray(
                new URL[urls.size()]);
    }

    private static URL[] copyClassPath(
            final URL[] classPath) {
        Objects.requireNonNull(
                classPath,
                "classPath");
        final URL[] copy =
                classPath.clone();
        for (URL entry : copy) {
            Objects.requireNonNull(
                    entry,
                    "classPath entry");
        }
        return copy;
    }
}
