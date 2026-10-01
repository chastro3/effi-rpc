plugins {
    id("java-library")
}
description = "Core specifications and configurations."
dependencies {
    api(project(":effi-rpc-component"))
    api(project(":effi-rpc-metrics"))
}
