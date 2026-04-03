group = "net.potatocloud.plugin.server.shared"

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.minimessage)
    implementation(libs.simpleyaml)
}
