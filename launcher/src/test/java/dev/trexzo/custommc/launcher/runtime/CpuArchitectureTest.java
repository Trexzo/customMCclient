package dev.trexzo.custommc.launcher.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CpuArchitectureTest {
    @Test
    void detectsCommonArchitectures() {
        assertEquals(
                CpuArchitecture.X64,
                CpuArchitecture.detect("amd64"));
        assertEquals(
                CpuArchitecture.X64,
                CpuArchitecture.detect("x86_64"));
        assertEquals(
                CpuArchitecture.X86,
                CpuArchitecture.detect("x86"));
        assertEquals(
                CpuArchitecture.ARM64,
                CpuArchitecture.detect("aarch64"));
    }

    @Test
    void legacyClassifierTokenIsExplicitlyUnsupportedOnArm() {
        assertThrows(
                IllegalStateException.class,
                () -> CpuArchitecture.ARM64.classifierToken());
        assertTrue(CpuArchitecture.X86.matchesRule("x86"));
        assertTrue(CpuArchitecture.X64.matchesRule("amd64"));
    }
}
