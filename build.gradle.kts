plugins {
    id("java")
}

group = "net.potatocloud"
version = "1.4.0"

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}