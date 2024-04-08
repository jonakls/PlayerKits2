plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}


repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.papermc.io/repository/maven-public/")
}


dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.mojang:authlib:1.5.25")
    implementation("com.zaxxer:HikariCP:4.0.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveBaseName.set("PlayerKitsRev")
    }
}

tasks.test {
    useJUnitPlatform()
}