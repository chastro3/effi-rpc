plugins {
    id("java-library")
}

description = "Transport API contracts, codecs, and exchange handlers."
dependencies {
    compileOnly("at.yawk.lz4:lz4-java")
    compileOnly("org.xerial.snappy:snappy-java")
    api(project(":effi-rpc-context"))
    api(project(":effi-rpc-marshalling"))
}
