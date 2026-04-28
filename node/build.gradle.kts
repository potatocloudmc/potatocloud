import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.node"

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":api"))
    implementation(project(":connector"))
    implementation(project(":common"))

    implementation(libs.commons.codec)

    implementation(libs.gson)
    implementation(libs.hikaricp)
    implementation(libs.mariadb)
    implementation(libs.jline)
    implementation(libs.oshi)
    implementation(libs.slf4j.nop)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.simpleyaml) {
        exclude(group = "org.yaml", module = "snakeyaml")
    }

    compileOnly(project(":platform-plugins:spigot"))
    compileOnly(project(":platform-plugins:spigot-legacy"))
    compileOnly(project(":platform-plugins:velocity"))
    compileOnly(project(":platform-plugins:limbo"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "net.potatocloud.node.NodeMain"
    }

    from(project(":platform-plugins:spigot").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":platform-plugins:spigot-legacy").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":platform-plugins:velocity").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":platform-plugins:limbo").tasks.named("shadowJar")) {
        into("default-files")
    }
}