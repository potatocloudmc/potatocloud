import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://dist.labymod.net/api/v1/maven/release/")
}

dependencies {
    compileOnly(project(":api"))
    implementation(project(":common"))
    implementation(project(":plugins:shared"))
    implementation(libs.labymod.common)
    implementation(libs.labymod.bukkit)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.spigot)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-labymod")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")
}

