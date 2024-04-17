plugins {
    id("java")
}

val paperApi = libs.paper
val messageLib = libs.bundles.nmessage

subprojects {
    apply(plugin = "java")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.unnamed.team/repository/unnamed-public/")
    }

    dependencies {
        compileOnly(paperApi)
        compileOnly(messageLib)

        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }
}