import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.module.template"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-module-template")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}