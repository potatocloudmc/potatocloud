import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
    id("net.fabricmc.fabric-loom") version "1.16.3"
}

val shaded = configurations.create("shaded")
configurations.implementation {
    extendsFrom(shaded)
}

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")

    implementation(libs.fabric.loader)

    shaded(project(":api"))
    shaded(project(":connector"))
    shaded(project(":common"))
    shaded(project(":network"))
    shaded(project(":eventbus"))
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shaded)
    archiveBaseName.set("potatocloud-plugin-fabric-26.1")
    archiveVersion.set("")
    archiveClassifier.set("")
    relocate("io.netty", "net.potatocloud.shaded.netty")
}
