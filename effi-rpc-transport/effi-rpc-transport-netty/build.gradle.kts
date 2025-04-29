description = "Transport implementation using Netty."
dependencies {
    api(project(":effi-rpc-transport:effi-rpc-transport-api"))
    api("io.netty:netty-codec-http2")
}