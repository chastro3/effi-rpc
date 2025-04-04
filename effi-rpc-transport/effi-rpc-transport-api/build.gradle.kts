description = "Defining transport layer api."
dependencies {
    compileOnly("org.lz4:lz4-java")
    compileOnly("org.xerial.snappy:snappy-java")
    api(project(":effi-rpc-serialization:effi-rpc-serialization-api"))
    api(project(":effi-rpc-metrics"))
    api("io.netty:netty-codec-http2")
}