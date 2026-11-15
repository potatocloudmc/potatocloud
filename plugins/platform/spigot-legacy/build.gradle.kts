import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

sourceSets.main {
    java.srcDir(project(":plugins:platform:spigot").file("src/main/java"))
}

dependencies {
    implementation(project(":api"))
    implementation(project(":connector"))

    compileOnly(libs.spigot)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-spigot-legacy")
    archiveVersion.set("")
    archiveClassifier.set("")

    relocate("io.netty", "net.potatocloud.shaded.netty")
}