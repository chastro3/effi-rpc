description = "Serialization implementation using MsgPack."
dependencies {
    api(project(":effi-rpc-serialization:effi-rpc-serialization-json"))
    api("org.msgpack:jackson-dataformat-msgpack")
}