plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "at.toastiii.craftflowers"
version = "2.0.0"

repositories {
    mavenCentral()
    maven(url = uri("https://repo.papermc.io/repository/maven-public/"))
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.128-stable")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.15.3")
    implementation("fr.minuskube.inv:smart-invs:1.2.7") {
        isTransitive = false
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<ProcessResources> {
    val pluginVersion = project.version.toString()
    inputs.property("version", pluginVersion)
    filesMatching("plugin.yml") {
        expand(mapOf("version" to pluginVersion))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("fr.minuskube.inv", "cm.ptks.craftflowers.smartinvs")
    }
    jar {
        archiveClassifier.set("plain")
    }
    build {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

tasks {
    runServer {
        minecraftVersion("26.2")
    }
}
