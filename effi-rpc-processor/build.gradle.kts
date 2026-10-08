plugins {
    id("java-library")
}
description = "Compile-time annotation processing for Effi RPC metadata and native-image configuration."
dependencies {
    api(project(":effi-rpc-annotation"))
}

