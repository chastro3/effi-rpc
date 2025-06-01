plugins {
    id("java-library")
}
description = "Data serialization and compression support."
dependencies {
    api(project(":effi-rpc-config"))
    // serialization
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("com.esotericsoftware:kryo")
    compileOnly("org.msgpack:jackson-dataformat-msgpack")
    compileOnly("com.google.protobuf:protobuf-java")
    compileOnly("com.google.protobuf:protobuf-java-util")
    // compression
    compileOnly("org.lz4:lz4-java")
    compileOnly("org.xerial.snappy:snappy-java")
}