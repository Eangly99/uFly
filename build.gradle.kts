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
    maven("https://mvn.intellectualsites.com/content/repositories/releases/")
    maven("https://mvn.intellectualsites.com/content/repositories/snapshots/")
}

configurations.compileClasspath {
    resolutionStrategy {
        force("com.google.code.gson:gson:2.11.0")
        force("com.google.guava:guava:33.3.1-jre")
        force("it.unimi.dsi:fastutil:8.5.15")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnly("com.intellectualsites.plotsquared:plotsquared-core:7.4.1") { isTransitive = false }
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit:7.4.1") { isTransitive = false }

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.11") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
}

tasks {
    // paperweight 2.x: reobfJar depends on jar automatically via the plugin.
    // Just wire shadowJar -> reobfJar and make assemble depend on shadowJar.
    shadowJar {
        archiveClassifier.set("")
    }

    reobfJar {
        dependsOn(jar)
    }

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
}
