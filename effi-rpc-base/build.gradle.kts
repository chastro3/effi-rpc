plugins {
    id("java-library")
}
description = "Core specifications and configurations."
dependencies {
    api(project(":effi-rpc-config"))
}
