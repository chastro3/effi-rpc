description="HTTP protocol support."
dependencies {
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    api(project(":effi-rpc-boot"))
    api(project(":effi-rpc-protocols:effi-rpc-http"))
}