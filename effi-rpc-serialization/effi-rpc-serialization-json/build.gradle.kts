description = "Serialization implementation using Jackson."
dependencies {
    api(project(":effi-rpc-serialization:effi-rpc-serialization-api"))
    api("com.fasterxml.jackson.core:jackson-databind")
}