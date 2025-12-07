group = "net.potatocloud.connector"

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}
