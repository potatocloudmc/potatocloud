import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

group = "net.potatocloud.node"

dependencies {
    implementation(project(":api"))
    implementation(project(":connector"))
    implementation(project(":common"))
    implementation(project(":network"))
    implementation(project(":eventbus"))

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)

    implementation(libs.jline)
    implementation(libs.oshi)
    implementation(libs.slf4j.nop)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(project(":plugins:platform:spigot"))
    compileOnly(project(":plugins:platform:spigot-legacy"))
    compileOnly(project(":plugins:platform:velocity"))
    compileOnly(project(":plugins:platform:limbo"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "net.potatocloud.node.NodeMain"
    }

    from(project(":plugins:platform:spigot").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:spigot-legacy").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:velocity").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:limbo").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:fabric:1_21_11").tasks.named("remapJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:fabric:26_1").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:neoforge:1_21_11").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugins:platform:neoforge:26_1").tasks.named("shadowJar")) {
        into("default-files")
    }
}