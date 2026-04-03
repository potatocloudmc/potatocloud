import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.plugin.server.cloudcommand"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public")
}

dependencies {
    compileOnly(project(":api"))
    implementation(project(":core"))
    implementation(project(":server-plugins:shared"))
    implementation(project(":common"))
    implementation(libs.simpleyaml)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.velocity)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-plugin-cloudcommand")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")

    exclude("net/potatocloud/api/**")
}


