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
        options.compilerArgs.addAll(
            listOf("-Xlint:all", "-Xlint:-options", "-Werror")
        )
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

tasks.register("verifyFoundation") {
    group = "verification"
    description = "Runs all foundation verification gates."
    dependsOn(
        ":core:check",
        ":platform-api:check",
        ":platform-1.8.9:check",
        ":launcher:check"
    )
}
