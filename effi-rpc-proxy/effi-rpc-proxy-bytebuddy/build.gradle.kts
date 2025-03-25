description = "Proxy implemented with bytebuddy."
dependencies {
    api(project(":effi-rpc-proxy:effi-rpc-proxy-api"))
    api("net.bytebuddy:byte-buddy")
}