pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ParkingDetection"

include(":android")
include(":android:domain")
include(":android:data")
include(":android:app")

project(":android").projectDir = file("android")
project(":android:domain").projectDir = file("android/domain")
project(":android:data").projectDir = file("android/data")
project(":android:app").projectDir = file("android/app")
