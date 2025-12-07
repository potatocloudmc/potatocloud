import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.plugins.notify"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public")
}

dependencies {
    compileOnly(project(":api"))
    implementation(libs.simpleyaml)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.velocity)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-notify")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")
}


