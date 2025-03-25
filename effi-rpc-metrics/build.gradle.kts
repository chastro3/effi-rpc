plugins {
    id("java-library")
}
description = "Provide effi rpc metrics."
dependencies {
    api(project(":effi-rpc-contract"))
}