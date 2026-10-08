plugins {
    id("java-library")
}
description = "Default bootstrap, lifecycle, registry, and protocol integration for Effi RPC."
dependencies {
    api(project(":effi-rpc-context"))
    api(project(":effi-rpc-transport:effi-rpc-transport-api"))
    api(project(":effi-rpc-governance"))
    api(project(":effi-rpc-proxy"))
}
