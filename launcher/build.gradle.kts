plugins {
    application
}

application {
    mainClass.set("dev.trexzo.custommc.launcher.LauncherMain")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":platform-api"))
    implementation("com.google.code.gson:gson:2.14.0")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
