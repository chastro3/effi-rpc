plugins {
    id("java-library")
}
description = "Framework annotations for RPC contracts and extension metadata."
dependencies {
    api(project(":effi-rpc-common"))
}
