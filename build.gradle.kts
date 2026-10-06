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

tasks.register("verifyFoundation") {
    group = "verification"
    description = "Runs all foundation verification gates."
    dependsOn(
        ":bootstrap:check",
        ":core:check",
        ":platform-api:check",
        ":platform-1.8.9:check",
        ":launcher:check",
        assembleRuntimeOverlay
    )
}
