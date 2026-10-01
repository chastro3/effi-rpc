plugins {
    id("java-library")
}

dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    api("org.springframework.boot:spring-boot-autoconfigure")
}
