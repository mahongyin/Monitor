@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("./localmaven")
        }
        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven {
//            setUrl("https://jitpack.io")
//            content {//意思只取他自己的
//                includeGroupByRegex("com.github.leavesCZY.*")
//            }
//        }
        maven {
            url = uri("./localmaven")
        }
        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

rootProject.name = "Monitor"
include(":app")
include(":monitor")
include(":monitor-plugin")
include(":monitor-no-op")