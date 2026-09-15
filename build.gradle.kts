plugins {
    id("net.fabricmc.fabric-loom")
    id("org.jetbrains.kotlin.jvm")
}

val minecraft_version: String by project
val loader_version: String by project
val fabric_api_version: String by project
val fabric_kotlin_version: String by project
val vaw_api_version: String by project
val mod_version: String by project
val maven_group: String by project

group = maven_group
version = mod_version

base {
    archivesName.set("villagers-at-work-mca")
}

repositories {
    // Public sources only; nothing here needs credentials.
    // -PvawApiRepository=mavenLocal builds against an api published locally from the core repository.
    if (providers.gradleProperty("vawApiRepository").orNull == "mavenLocal") {
        mavenLocal()
    }
    maven("https://msameer.github.io/maven/") {
        name = "VillagersAtWork"
        content { includeGroup("dev.msameer.vaw") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    implementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    // Provided at runtime by the Villagers at Work core jar, which nests the api.
    compileOnly("dev.msameer.vaw:vaw-api:$vaw_api_version")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_villagers-at-work-mca" }
    }
}
