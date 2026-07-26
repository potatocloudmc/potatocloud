import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
    id("net.neoforged.moddev") version "2.0.142"
}

val shaded = configurations.create("shaded")
shaded.exclude(group = "io.netty")
configurations.implementation {
    extendsFrom(shaded)
}

repositories {
    mavenCentral()
}

neoForge {
    enable {
        version = "21.11.44"
        isDisableRecompilation = true
    }
}

dependencies {
    shaded(project(":api"))
    shaded(project(":connector"))
    shaded(project(":common"))
    shaded(project(":network"))
    shaded(project(":eventbus"))
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shaded)
    mergeServiceFiles()
    archiveBaseName.set("potatocloud-plugin-neoforge-1.21.11")
    archiveVersion.set("")
    archiveClassifier.set("")
    relocate("tools.jackson", "net.potatocloud.shaded.jackson")
    relocate("com.fasterxml.jackson", "net.potatocloud.shaded.jackson.annotation")
}
