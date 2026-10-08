plugins {
    id("java-library")
}

description = "Spring Boot auto-configuration for Effi RPC."

dependencies {
    api(project(":effi-rpc-protocols:effi-rpc-http"))
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
