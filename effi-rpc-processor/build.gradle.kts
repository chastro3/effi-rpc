plugins {
    id("java-library")
}
description = "Compile-time annotation processing and native-image configuration."
dependencies {
    api(project(":effi-rpc-annotation"))
}

