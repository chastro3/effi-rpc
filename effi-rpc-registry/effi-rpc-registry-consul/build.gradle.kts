description = "Service registry implemented with consul."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("io.vertx:vertx-consul-client")
}