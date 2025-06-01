description = "Transport layer API definitions."
dependencies {
    compileOnly("org.lz4:lz4-java")
    compileOnly("org.xerial.snappy:snappy-java")
    api(project(":effi-rpc-marshalling"))
    api(project(":effi-rpc-metrics"))
}