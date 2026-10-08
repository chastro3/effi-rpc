plugins {
    id("java-library")
}

description = "Consul registry implementation for Effi RPC."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("io.vertx:vertx-consul-client")
    api("org.kiwiproject:consul-client")
}
