import org.apache.tools.ant.filters.FixCrLfFilter

plugins {
    id("java")
}

group = "net.potatocloud"
version = "2.0.0"

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

val velocityPlugins = listOf("cloudcommand", "hub", "notify", "proxy")
val paperPlugins = listOf("labymod", "npc", "sign")
val nodeModules = listOf("rest")

val distribution = tasks.register<Zip>("distribution") {
    group = "distribution"
    description = "Builds the release zip."
    archiveFileName.set("potatocloud-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(project(":node").tasks.named("shadowJar")) {
        rename { "potatocloud.jar" }
    }

    from("dist") {
        include("start.bat")
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
    }

    from("dist") {
        include("start.sh")
        filePermissions { unix("rwxr-xr-x") }
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("lf"))
    }

    into("optional/velocity-plugins") {
        velocityPlugins.forEach { from(project(":plugins:optional:$it").tasks.named("shadowJar")) }
    }
    into("optional/paper-plugins") {
        paperPlugins.forEach { from(project(":plugins:optional:$it").tasks.named("shadowJar")) }
    }
    into("optional/node-modules") {
        nodeModules.forEach { from(project(":modules:$it").tasks.named("shadowJar")) }
    }
}

tasks.named("build") {
    dependsOn(distribution)
}