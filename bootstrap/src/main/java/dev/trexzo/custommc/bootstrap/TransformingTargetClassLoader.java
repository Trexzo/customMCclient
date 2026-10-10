package dev.trexzo.custommc.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.jar.JarFile;

public final class TransformingTargetClassLoader
        extends URLClassLoader {
    private static final int BUFFER_SIZE = 8192;

    private final BootstrapClassTransformer transformer;
    private final Set<String> vanillaClassNames;

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
        // All classes from the original game JAR must share one classloader.
        // Parent-loaded WorldClient is not a subclass of child-loaded World.
        this.vanillaClassNames = discoverVanillaClasses(getURLs());
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

        final boolean claimed = transformer.handles(name);
        if (!claimed && !vanillaClassNames.contains(name)) {
            return super.loadClass(
                    name,
                    resolve);
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded =
                    findLoadedClass(name);
            if (loaded == null) {
                loaded = claimed
                        ? findTransformedClass(name)
                        : findClass(name);
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

        final VerifiedOriginal original;
        try {
            original =
                    readVerifiedOriginal(resource);
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
                                    original.bytes),
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

        // Retain the original JAR's *verified* signer certificates. Without
        // the matching CodeSource, loading unsigned transformed classes beside
        // normally loaded signed Mojang classes in the default package throws
        // a SecurityException before actual JVM bytecode linkage can run.
        // The verification happens while reading the entire original entry;
        // we never invent or bypass signatures and never modify the JAR.
        if (original.codeSource != null) {
            return defineClass(binaryName, transformed, 0,
                    transformed.length, original.codeSource);
        }
        return defineClass(binaryName, transformed, 0,
                transformed.length);
    }

    /**
     * Discover exact vanilla class names using the signed game's own archive,
     * never guessing obfuscated prefixes or child-loading third-party libs.
     * Claimed classes are transformed; other vanilla owners are loaded
     * unchanged with URLClassLoader's normal signed-JAR verification.
     */
    private static Set<String> discoverVanillaClasses(final URL[] classPath) {
        Set<String> found = null;
        for (URL url : classPath) {
            if (!"file".equals(url.getProtocol())) {
                continue;
            }
            final File file;
            try {
                file = new File(url.toURI());
            } catch (java.net.URISyntaxException invalid) {
                throw new IllegalArgumentException("invalid classpath URI", invalid);
            }
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }
            try (JarFile jar = new JarFile(file)) {
                if (jar.getJarEntry("net/minecraft/client/main/Main.class") == null
                        || jar.getJarEntry("ave.class") == null) {
                    continue;
                }
                if (found != null) {
                    throw new IllegalArgumentException(
                            "multiple Minecraft game JARs on runtime classpath");
                }
                found = new LinkedHashSet<String>();
                final Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (!entry.isDirectory() && name.endsWith(".class")) {
                        found.add(name.substring(0, name.length() - 6)
                                .replace('/', '.'));
                    }
                }
            } catch (IOException invalid) {
                // Unrelated classpath JARs cannot establish vanilla ownership.
                continue;
            }
        }
        return found == null
                ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(found);
    }

    private static VerifiedOriginal readVerifiedOriginal(
            final URL resource) throws IOException {
        final URLConnection connection = resource.openConnection();
        final byte[] bytes;
        try (InputStream input = connection.getInputStream();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            bytes = output.toByteArray();
        }
        if (connection instanceof JarURLConnection) {
            final JarURLConnection jar = (JarURLConnection) connection;
            // Only after consuming the verified entry can its signer
            // certificates be trusted. A corrupt signed entry fails during
            // the read and is never supplied to the transformer.
            final JarEntry entry = jar.getJarEntry();
            if (entry == null) {
                throw new IOException("target class JAR entry missing");
            }
            final Certificate[] certificates = entry.getCertificates();
            return new VerifiedOriginal(bytes,
                    new CodeSource(jar.getJarFileURL(), certificates));
        }
        return new VerifiedOriginal(bytes, null);
    }

    private static final class VerifiedOriginal {
        final byte[] bytes;
        final CodeSource codeSource;

        VerifiedOriginal(final byte[] bytes, final CodeSource codeSource) {
            this.bytes = bytes;
            this.codeSource = codeSource;
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
