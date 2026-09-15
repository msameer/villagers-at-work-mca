pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }

    val loom_version: String by settings
    val kotlin_version: String by settings
    plugins {
        id("net.fabricmc.fabric-loom") version loom_version
        id("org.jetbrains.kotlin.jvm") version kotlin_version
    }
}

rootProject.name = "villagers-at-work-mca"
