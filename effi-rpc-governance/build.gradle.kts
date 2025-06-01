plugins {
    id("java-library")
}
description = "Service governance, routing and load balancing."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api(project(":effi-rpc-metrics"))
}