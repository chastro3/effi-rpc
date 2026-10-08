plugins {
    id("java-library")
}
description = "Metrics contracts and aggregation for Effi RPC."
dependencies {
    api(project(":effi-rpc-common"))
    api(project(":effi-rpc-annotation"))
}
