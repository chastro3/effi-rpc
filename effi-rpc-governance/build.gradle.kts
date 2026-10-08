plugins {
    id("java-library")
}
description = "Service discovery, routing, and load balancing for Effi RPC."
dependencies {
    api(project(":effi-rpc-context"))
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
}
