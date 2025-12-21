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

    implementation(libs.simpleyaml) {
        exclude(group = "org.yaml", module = "snakeyaml")
    }

    implementation(libs.commons.codec)
    implementation(libs.commons.io)
    implementation(libs.gson)
    implementation(libs.jline)
    implementation(libs.oshi)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(project(":plugin-spigot"))
    compileOnly(project(":plugin-velocity"))
    compileOnly(project(":plugin-limbo"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud")
    archiveVersion.set("${rootProject.version}")
    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "net.potatocloud.node.NodeMain"
    }

    from(project(":plugin-spigot").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugin-velocity").tasks.named("shadowJar")) {
        into("default-files")
    }
    from(project(":plugin-limbo").tasks.named("shadowJar")) {
        into("default-files")
    }
}