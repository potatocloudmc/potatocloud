group = "net.potatocloud.core"

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.netty.handler)
    implementation(libs.netty.epoll)
    implementation(libs.gson)
}
