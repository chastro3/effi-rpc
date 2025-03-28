dependencies {
    implementation(project(":effi-rpc-engine:effi-rpc-engine-http"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    implementation(project(":effi-rpc-serialization:effi-rpc-serialization-json"))
    implementation("org.slf4j:slf4j-api")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.16")
}
