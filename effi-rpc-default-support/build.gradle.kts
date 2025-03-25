plugins {
    id("java-library")
}
dependencies {
    api(project(":effi-rpc-protocol:effi-rpc-protocol-api"))
    api(project(":effi-rpc-proxy:effi-rpc-proxy-api"))
    api("org.ow2.asm:asm")
}