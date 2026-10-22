import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public")
}

dependencies {
    compileOnly(project(":api"))
    implementation(project(":common"))
    implementation(project(":plugins:shared"))

    compileOnly(libs.paper)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-npc")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")
}
