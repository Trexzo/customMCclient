plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

dependencies {
    api(project(":platform-api"))

    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        isTransitive = false
    }
    testRuntimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        isTransitive = false
    }

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
