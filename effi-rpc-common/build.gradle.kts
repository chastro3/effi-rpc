plugins {
    id("java-library")
}
description = "Common components, utility classes, and constants."
dependencies {
    api(platform(project(":effi-rpc-bom")))
    api("org.jspecify:jspecify")
    api("com.lmax:disruptor")
    api("org.ow2.asm:asm")
    compileOnly("org.slf4j:slf4j-api")
    compileOnly("org.apache.logging.log4j:log4j-api")
    compileOnly("commons-logging:commons-logging")
}


tasks.withType<ProcessResources> {
    filesMatching("META-INF/effi-rpc/version") {
        expand("version" to project.version)
    }
}

