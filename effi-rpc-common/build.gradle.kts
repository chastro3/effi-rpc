plugins {
    id("java-library")
}
description = "Provide some commonly used components, utility classes, and constants."
dependencies {
    api(platform(project(":effi-rpc-bom")))
    api("com.lmax:disruptor")
    api("org.ow2.asm:asm")
    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.auto.service:auto-service:1.1.1")
    compileOnly("org.slf4j:slf4j-api")
    compileOnly("org.apache.logging.log4j:log4j-api")
    compileOnly("commons-logging:commons-logging")
    compileOnly("io.vertx:vertx-core")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
}


tasks.withType<ProcessResources> {
    filesMatching("META-INF/effi-rpc/version") {
        expand("version" to project.version)
    }
}

