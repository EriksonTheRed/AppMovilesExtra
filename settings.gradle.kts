pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {

    repositories {

        mavenLocal()

        google()

        mavenCentral()

        maven { url = uri("https://jitpack.io") }

    }
}

rootProject.name = "AppMoviles"
include(":app")

