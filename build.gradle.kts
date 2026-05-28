plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "dev.naruto"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    // PlotSquared v7+ is published to Maven Central via IntellectualSites BOM
    // No extra repo needed — mavenCentral() covers it
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    // PlotSquared v7 — use BOM to manage versions automatically
    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.55"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.11")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
    }
}
