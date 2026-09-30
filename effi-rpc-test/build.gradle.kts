plugins {
    id("java-library")
}

dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    testImplementation(project(":effi-rpc-integration:effi-rpc-spring-boot-starter"))
    implementation(project(":effi-rpc-proxy"))
    implementation("tools.jackson.core:jackson-databind")
    implementation("com.esotericsoftware:kryo")
    implementation("com.google.protobuf:protobuf-java")
    implementation("com.google.protobuf:protobuf-java-util")
    annotationProcessor(project(":effi-rpc-processor"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.glassfish.jersey.core:jersey-server")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.google.auto:auto-common")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    implementation("org.openjdk.jmh:jmh-core")
    annotationProcessor(platform(project(":effi-rpc-bom")))
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")
    testAnnotationProcessor(project(":effi-rpc-processor"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    enabled = true
    useJUnitPlatform()
}
