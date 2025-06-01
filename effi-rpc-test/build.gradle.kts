plugins {
    id("java-library")
}

dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    implementation(project(":effi-rpc-proxy"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.esotericsoftware:kryo")
    implementation("org.msgpack:jackson-dataformat-msgpack")
    implementation("com.google.protobuf:protobuf-java")
    implementation("com.google.protobuf:protobuf-java-util")
    annotationProcessor(project(":effi-rpc-processor"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.glassfish.jersey.core:jersey-server:3.1.10")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    enabled = false
    useJUnitPlatform()
}
