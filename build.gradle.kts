plugins {
    id("net.fabricmc.fabric-loom")
    id("org.jetbrains.kotlin.jvm")
}

val minecraft_version: String by project
val loader_version: String by project
val fabric_api_version: String by project
val fabric_kotlin_version: String by project
val vaw_api_version: String by project
val mca_version: String by project
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
    // MCA Reborn, which this extension compiles against and runs with in game tests (§16.1).
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

/**
 * The Villagers at Work core jar, for game tests only: `-PvawCoreJar=<path>`. The core is not
 * published, so game tests run locally against a jar built in the core repository, and a build
 * without one (CI) compiles the extension and skips them.
 */
val vawCoreJar: String? = providers.gradleProperty("vawCoreJar").orNull

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    implementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    // Provided at runtime by the Villagers at Work core jar, which nests the api.
    compileOnly("dev.msameer.vaw:vaw-api:$vaw_api_version")

    implementation("maven.modrinth:minecraft-comes-alive-reborn:$mca_version")

    if (vawCoreJar != null) {
        localRuntime(files(vawCoreJar))
    }
}

if (vawCoreJar != null) {
    fabricApi {
        configureTests {
            createSourceSet.set(true)
            modId.set("vaw-mca-test")
            enableGameTests.set(true)
            // The user accepted the Minecraft EULA for game-test runs.
            eula.set(true)
        }
    }
    dependencies {
        "gametestCompileOnly"("dev.msameer.vaw:vaw-api:$vaw_api_version")
    }
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
