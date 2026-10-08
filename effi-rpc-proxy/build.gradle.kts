plugins {
    id("java-library")
}
description = "Interface and object proxy generation."
dependencies {
    api(project(":effi-rpc-annotation"))
    compileOnly("org.springframework:spring-core")
    compileOnly("net.bytebuddy:byte-buddy")
}
