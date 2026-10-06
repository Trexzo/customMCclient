plugins {
    base
}

allprojects {
    group = "dev.trexzo.custommc"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-options", "-Werror"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

val runtimeOverlayDirectory =
    layout.buildDirectory.dir("runtime-overlay")

val assembleRuntimeOverlay by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Assembles the canonical CustomMC Minecraft 1.8.9 runtime overlay."

    dependsOn(
        ":bootstrap:jar",
        ":core:jar",
        ":platform-api:jar",
        ":platform-1.8.9:jar"
    )

    into(runtimeOverlayDirectory)

    from(
        project(":bootstrap").layout.buildDirectory.file(
            "libs/bootstrap-${project(":bootstrap").version}.jar"
        )
    ) {
        rename { "bootstrap.jar" }
    }

    from(
        project(":core").layout.buildDirectory.file(
            "libs/core-${project(":core").version}.jar"
        )
    ) {
        rename { "core.jar" }
    }

    from(
        project(":platform-api").layout.buildDirectory.file(
            "libs/platform-api-${project(":platform-api").version}.jar"
        )
    ) {
        rename { "platform-api.jar" }
    }

    from(
        project(":platform-1.8.9").layout.buildDirectory.file(
            "libs/platform-1.8.9-${project(":platform-1.8.9").version}.jar"
        )
    ) {
        rename { "platform-1.8.9.jar" }
    }
}


val launcherDistributionDirectory =
    layout.buildDirectory.dir("custommc-distribution")

val assembleLauncherDistribution by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Assembles the runnable CustomMC launcher plus canonical Minecraft 1.8.9 runtime overlay."

    dependsOn(
        ":launcher:installDist",
        assembleRuntimeOverlay
    )

    into(launcherDistributionDirectory)

    from(
        project(":launcher").layout.buildDirectory.dir(
            "install/launcher"
        )
    )

    from(runtimeOverlayDirectory) {
        into("runtime-overlay")
    }

    from("README.md")
}

val verifyLauncherDistribution by tasks.registering {
    group = "verification"
    description = "Verifies the assembled runnable launcher distribution shape."

    dependsOn(assembleLauncherDistribution)

    doLast {
        val root = launcherDistributionDirectory.get().asFile

        val required = listOf(
            "bin/launcher",
            "bin/launcher.bat",
            "runtime-overlay/bootstrap.jar",
            "runtime-overlay/core.jar",
            "runtime-overlay/platform-api.jar",
            "runtime-overlay/platform-1.8.9.jar",
            "README.md"
        )

        required.forEach { relative ->
            val file = root.resolve(relative)
            check(file.isFile) {
                "Missing launcher distribution file: $relative"
            }
        }

        val libs = root.resolve("lib")
        check(libs.isDirectory) {
            "Missing launcher distribution lib directory"
        }
        check(libs.listFiles()?.any { it.isFile && it.extension == "jar" } == true) {
            "Launcher distribution lib directory contains no jars"
        }

        val overlayNames = root.resolve("runtime-overlay")
            .listFiles()
            ?.filter { it.isFile }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        check(
            overlayNames == listOf(
                "bootstrap.jar",
                "core.jar",
                "platform-1.8.9.jar",
                "platform-api.jar"
            )
        ) {
            "Unexpected runtime overlay contents: $overlayNames"
        }
    }
}

tasks.register("verifyFoundation") {
    group = "verification"
    description = "Runs all foundation verification gates."
    dependsOn(
        ":bootstrap:check",
        ":core:check",
        ":platform-api:check",
        ":platform-1.8.9:check",
        ":launcher:check",
        assembleRuntimeOverlay,
        verifyLauncherDistribution
    )
}
