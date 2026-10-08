plugins {
    id("java-library")
}
description = "Metrics contracts and aggregation."
dependencies {
    api(project(":effi-rpc-common"))
    api(project(":effi-rpc-annotation"))
}
