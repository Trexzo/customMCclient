package dev.trexzo.custommc.bootstrap;

public interface BootstrapClassTransformer {
    boolean handles(String binaryClassName);

    byte[] transform(
            String binaryClassName,
            byte[] originalBytes)
            throws Exception;
}
