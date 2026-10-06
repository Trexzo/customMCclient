plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Main-Class"] =
            "dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
