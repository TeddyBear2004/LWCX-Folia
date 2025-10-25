import java.util.*

plugins {
    id("java-library")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(group = "dev.folia", name = "folia-api", version = "1.21.8-R0.1-SNAPSHOT")
    compileOnly(group = "com.sk89q.worldedit", name = "worldedit-bukkit", version = "7.3.16")
    compileOnly(group = "com.sk89q.worldguard", name = "worldguard-bukkit", version = "7.0.14")
    compileOnly(group = "com.palmergames.bukkit.towny", name = "towny", version = "0.98.2.0")
    compileOnly(group = "com.massivecraft", name = "Factions", version = "1.6.9.5-U0.4.9") {
        isTransitive = false
    }
    compileOnly(group = "com.github.MilkBowl", name = "VaultAPI", version = "1.7.1")
    compileOnly(group = "com.google.guava", name = "guava", version = "23.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.2")
    implementation("commons-lang:commons-lang:2.6")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    jar {
        archiveClassifier.set("noshade")
    }
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
            )
        }
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${rootProject.name.uppercase(Locale.getDefault())}-${project.version}.jar")
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

publishing {
    repositories {
        if (project.hasProperty("mavenUsername") && project.hasProperty("mavenPassword")) {
            maven {
                credentials {
                    username = "${project.property("mavenUsername")}"
                    password = "${project.property("mavenPassword")}"
                }
                url = uri("https://repo.codemc.io/repository/maven-releases/")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"
            from(components["java"])
        }
    }
}
tasks.register<Copy>("copyToServer") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)
    into("${project.rootDir}/server/plugins")
}

tasks.build {
    dependsOn("copyToServer")
}
