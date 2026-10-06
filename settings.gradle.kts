pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "customMCclient"

include(
    "bootstrap",
    "core",
    "launcher",
    "platform-api",
    "platform-1.8.9"
)
