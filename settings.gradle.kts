pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
            content {
                includeGroup("org.bukkit")
                includeGroup("org.spigotmc")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
        }
    }
}


rootProject.name = "starfarm-mylittlefarm"