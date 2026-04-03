import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.plugin.platform.velocity"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":connector"))
    implementation(project(":core"))
    implementation(project(":common"))

    compileOnly(libs.velocity)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-velocity")
    archiveVersion.set("")
    archiveClassifier.set("")
    relocate("io.netty", "net.potatocloud.shaded.netty")
}
