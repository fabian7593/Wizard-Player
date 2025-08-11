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
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

//TODO coment for include(":app") and rootProject.name = "WizardPlayerLibrary" upload library, and decomment for debbug project
rootProject.name = "WizardPlayerLibrary"
//rootProject.name = "wizardplayer"
//include(":app")
include(":WizardPlayerLibrary")
