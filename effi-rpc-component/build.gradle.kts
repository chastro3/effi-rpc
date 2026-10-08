plugins {
    id("java-library")
}
description = "Core container, extension, lifecycle, event, and infrastructure components for Effi RPC."
dependencies {
    api(project(":effi-rpc-annotation"))
    api(project(":effi-rpc-metrics"))
}
