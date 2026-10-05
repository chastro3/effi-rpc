plugins {
    id("java-library")
}
description = "Common components, utility classes, and constants."
dependencies {
    api(platform(project(":effi-rpc-bom")))
    api("org.jspecify:jspecify")
    api("org.ow2.asm:asm")
    // https://mvnrepository.com/artifact/org.jctools/jctools-core
    api("org.jctools:jctools-core")
    compileOnly("org.slf4j:slf4j-api")
    compileOnly("org.apache.logging.log4j:log4j-api")
    compileOnly("commons-logging:commons-logging")
}


val projectVersion = version.toString()

tasks.withType<ProcessResources>().configureEach {
    expand("version" to projectVersion)
}

