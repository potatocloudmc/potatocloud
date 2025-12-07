group = "net.potatocloud.core"

dependencies {
    implementation(project(":api"))

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.commons.io)
    implementation(libs.netty)
    implementation(libs.gson)
}
