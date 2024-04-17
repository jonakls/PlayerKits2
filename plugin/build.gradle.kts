plugins {
    id("java")
    alias(libs.plugins.shadow)
}


repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net/")
}


dependencies {
    implementation(project(":api"))

    compileOnly(libs.placeholder)
    compileOnly(libs.miniplaceholder)
    compileOnly(libs.vault)
    compileOnly(libs.mojang)

    implementation(libs.hikari)
    implementation(libs.bundles.nmessage)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveBaseName.set("PlayerKitsRev")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}