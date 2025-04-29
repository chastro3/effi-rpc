description = "Serialization implementation using Kryo."
dependencies {
    api(project(":effi-rpc-serialization:effi-rpc-serialization-api"))
    api("com.esotericsoftware:kryo")
}