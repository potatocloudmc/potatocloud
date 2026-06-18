plugins {
    alias(libs.plugins.shadow)
    kotlin("jvm")
    kotlin("plugin.lombok")
}

group = "net.potatocloud.module.webinterface"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}


dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":api"))
    compileOnly(project(":node"))

    implementation("io.javalin:javalin:7.1.0")
    runtimeOnly("io.javalin.community.openapi:javalin-swagger-plugin:7.2.2")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:7.1.0")

    implementation("org.bitbucket.b_c:jose4j:0.9.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.2")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(kotlin("test"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("potatocloud-module-webinterface")
    archiveVersion.set("")
    archiveClassifier.set("")

    mergeServiceFiles()
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    dependencies {
        exclude(dependency("net.potatocloud:api"))
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

kotlin {
    jvmToolchain(25)
}