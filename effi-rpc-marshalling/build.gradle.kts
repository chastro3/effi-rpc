plugins {
    id("java-library")
}
description = "Serialization and compression support."
dependencies {
    api(project(":effi-rpc-component"))
    // serialization
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("com.esotericsoftware:kryo")
    compileOnly("com.google.protobuf:protobuf-java")
    compileOnly("com.google.protobuf:protobuf-java-util")
    // compression
    compileOnly("at.yawk.lz4:lz4-java")
    compileOnly("org.xerial.snappy:snappy-java")
}
