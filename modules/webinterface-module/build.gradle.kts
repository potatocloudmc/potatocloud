plugins {
    id("java")
    id("io.quarkus") version "3.36.2"
}

group = "net.potatocloud"
version = "2.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-proc:full")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.36.2"))
}


dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":api"))
    compileOnly(project(":node"))
    
    // REST layer
    implementation("io.quarkus:quarkus-rest:3.36.2")
    implementation("io.quarkus:quarkus-rest-jackson:3.36.2")

    // Bean Validation
    implementation("io.quarkus:quarkus-hibernate-validator:3.36.2")

    // WebSockets
    implementation("io.quarkus:quarkus-websockets-next:3.36.2")

    // OpenAPI
    implementation("io.quarkus:quarkus-smallrye-openapi:3.36.2")

    // Security
    implementation("io.quarkus:quarkus-security:3.36.2")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // Lombok + MapStruct binding
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Tests
    testImplementation("io.quarkus:quarkus-junit5:3.36.2")
    testImplementation("io.rest-assured:rest-assured:6.0.0")
}

