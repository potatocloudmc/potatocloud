dependencies {
    implementation(project(":common"))
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.minimessage)

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
}
