plugins {
    id("java-library")
}
description = "RPC metrics collection."
dependencies {
    api(project(":effi-rpc-context"))
}