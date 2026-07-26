import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
    id("net.fabricmc.fabric-loom-remap") version "1.16.3"
}

val shaded = configurations.create("shaded")
configurations.implementation {
    extendsFrom(shaded)
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabric.loader)

    shaded(project(":api"))
    shaded(project(":connector"))
    shaded(project(":common"))
    shaded(project(":network"))
    shaded(project(":eventbus"))
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shaded)
    destinationDirectory.set(layout.buildDirectory.dir("remap-input"))
    archiveBaseName.set("potatocloud-plugin-fabric")
    archiveVersion.set("")
    archiveClassifier.set("bundled")
    relocate("io.netty", "net.potatocloud.shaded.netty")
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    archiveFileName.set("potatocloud-plugin-fabric-1.21.11.jar")
}
