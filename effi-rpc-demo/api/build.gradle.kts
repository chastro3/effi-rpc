plugins {
    id("java-library")
}

description = "Shared service contracts for RPC demos."

dependencies {
    implementation(platform(project(":effi-rpc-bom")))
}
