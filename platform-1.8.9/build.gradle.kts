plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

dependencies {
    api(project(":platform-api"))
    api(project(":bootstrap"))

    implementation("org.ow2.asm:asm:9.7.1")

    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        isTransitive = false
    }
    testRuntimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        isTransitive = false
    }

    // Vanilla 1.8.9 Main resolves jopt-simple before main() executes.
    // The launcher normally supplies it from Mojang version metadata;
    // this belongs only to the genuine-entrypoint test runtime.
    testRuntimeOnly("net.sf.jopt-simple:jopt-simple:4.6")

    testImplementation("org.ow2.asm:asm-tree:9.7.1")
    testImplementation("org.ow2.asm:asm-analysis:9.7.1")
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

// Surface the actual JVM verifier cause in hosted raycast acceptance CI.
tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
