group = "net.potatocloud.api"

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}

publishing {
    publications {
        create<MavenPublication>("api") {
            from(components["java"])
            groupId = "net.potatocloud.api"
            artifactId = "api"
            version = rootProject.version.toString()
        }
    }
}
