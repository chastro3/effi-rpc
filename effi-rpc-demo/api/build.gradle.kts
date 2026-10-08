plugins {
    id("java-library")
}

description = "Shared service contracts for Effi RPC demos."

dependencies {
    implementation(platform(project(":effi-rpc-bom")))
}
