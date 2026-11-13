import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.module.rest"

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":common"))
    compileOnly(libs.jackson.core)
    compileOnly(libs.jackson.databind)
    compileOnly(libs.jackson.yaml)

    implementation(libs.javalin) {
        exclude(group = "org.slf4j")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-module-rest")
    archiveVersion.set("")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}
