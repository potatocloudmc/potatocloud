group = "net.potatocloud.connector"

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":common"))

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}
