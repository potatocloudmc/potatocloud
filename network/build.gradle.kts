group = "net.potatocloud.network"

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))

    implementation(libs.netty.handler)
    implementation(libs.netty.epoll)
    runtimeOnly(libs.netty.pkitesting)
}