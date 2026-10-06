package dev.trexzo.custommc.launcher;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.launcher.launch.Minecraft189LaunchCli;
import dev.trexzo.custommc.launcher.runtime.InstallationInspection;
import dev.trexzo.custommc.launcher.runtime.MinecraftHomeLocator;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallationProbe;
import dev.trexzo.custommc.platform.PlatformContext;

import java.nio.file.Path;
import java.util.Arrays;

public final class LauncherMain {
    private LauncherMain() {
    }

    public static void main(final String[] args)
            throws Exception {
        if (args.length > 0) {
            if (!"launch".equals(args[0])) {
                throw new IllegalArgumentException(
                        "unknown launcher command: "
                                + args[0]);
            }

            final int exitCode =
                    Minecraft189LaunchCli.run(
                            Arrays.copyOfRange(
                                    args,
                                    1,
                                    args.length));
            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Minecraft exited with code "
                                + exitCode);
            }
            return;
        }

        final EventBus events = new EventBus();
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController moduleController =
                new ModuleController(modules);
        final ServiceRegistry services = new ServiceRegistry();

        final PlatformContext context = new PlatformContext(
                events,
                modules,
                moduleController,
                services);

        final Path minecraftHome =
                new MinecraftHomeLocator().locateCurrent(null);
        final MinecraftInstallation installation =
                MinecraftInstallation.forVersion(
                        minecraftHome,
                        "1.8.9");
        final InstallationInspection inspection =
                new MinecraftInstallationProbe().inspect(
                        installation);

        System.out.println("customMCclient foundation");
        System.out.println(
                "core.modules="
                        + context.modules().snapshot().size());
        System.out.println(
                "core.services="
                        + context.services().snapshot().size());
        System.out.println(
                "minecraft.home=" + minecraftHome);
        System.out.println(
                "minecraft.version=" + installation.versionId());
        System.out.println(
                "minecraft.launchReady="
                        + inspection.launchReady());
    }
}
