import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://dist.labymod.net/api/v1/maven/release/")
}

dependencies {
    compileOnly(project(":api"))
    implementation(project(":plugins:shared"))
    implementation(project(":common"))
    implementation(libs.labymod.common)
    implementation(libs.labymod.velocity)

    compileOnly(libs.velocity)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-proxy")
    archiveVersion.set("")
    archiveClassifier.set("")
}


