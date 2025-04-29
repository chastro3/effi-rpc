description = "Service registry implementation using Consul."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("io.vertx:vertx-consul-client")
}