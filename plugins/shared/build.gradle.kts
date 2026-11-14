dependencies {
    implementation(project(":common"))
    compileOnly(libs.minimessage)

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
}
