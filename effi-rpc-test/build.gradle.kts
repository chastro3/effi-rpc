plugins {
    id("java-library")
}

dependencies {
    compileOnly(project(":effi-rpc-common"))
    testImplementation(project(":effi-rpc-engine:effi-rpc-engine-http"))
    testImplementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    testImplementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    testImplementation(project(":effi-rpc-proxy:effi-rpc-proxy-jdk"))
    testImplementation("jakarta.ws.rs:jakarta.ws.rs-api")
    testImplementation("org.glassfish.jersey.core:jersey-server:3.1.10")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    enabled = false
    useJUnitPlatform()
}
