plugins {
    id("java-library")
}
description = "Module integration and default implementation support."
dependencies {
    api(project(":effi-rpc-transport:effi-rpc-transport-api"))
    api(project(":effi-rpc-governance"))
    api(project(":effi-rpc-proxy"))
}