plugins {
    id("java-library")
}
description = "Service governance, routing, load balancing, fault tolerance, etc."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
}