import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.plugins.proxy"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://dist.labymod.net/api/v1/maven/release/")
}

dependencies {
    compileOnly(project(":api"))
    implementation(libs.simpleyaml)
    implementation(libs.labymod.common)
    implementation(libs.labymod.velocity)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.velocity)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-proxy")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")
}


