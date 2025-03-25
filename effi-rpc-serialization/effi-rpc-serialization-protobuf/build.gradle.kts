description = "Serialization implemented with protobuf."
dependencies {
    api(project(":effi-rpc-serialization:effi-rpc-serialization-api"))
    api("com.google.protobuf:protobuf-java")
    api("com.google.protobuf:protobuf-java-util")
}