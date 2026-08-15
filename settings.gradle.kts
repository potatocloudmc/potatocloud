pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "potatocloud"
include("api")
include("connector")
include("node")
include("common")
include("modules")
include("modules:module-template")
include("network")
include("eventbus")
include("plugins")
include("plugins:shared")
include("plugins:platform")
include("plugins:platform:limbo")
include("plugins:platform:spigot")
include("plugins:platform:spigot-legacy")
include("plugins:platform:velocity")
include("plugins:platform:fabric:1_21_11")
include("plugins:platform:fabric:26_1")
include("plugins:platform:neoforge:1_21_11")
include("plugins:platform:neoforge:26_1")
include("plugins:optional")
include("plugins:optional:cloudcommand")
include("plugins:optional:hub")
include("plugins:optional:labymod")
include("plugins:optional:notify")
include("plugins:optional:proxy")
