import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.plugin.limbo"

repositories {
    maven("https://repo.loohpjames.com/repository")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":connector"))

    compileOnly(libs.limbo)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-limbo")
    archiveVersion.set("")
    archiveClassifier.set("")
}