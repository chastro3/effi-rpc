description = "Serialization implemented with msgpack."
dependencies {
    api(project(":effi-rpc-serialization:effi-rpc-serialization-json"))
    api("org.msgpack:jackson-dataformat-msgpack")
}