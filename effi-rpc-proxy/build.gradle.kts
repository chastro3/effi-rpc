plugins {
    id("java-library")
}
description  = "Proxy generation for interfaces and instance."
dependencies{
    api(project(":effi-rpc-annotation"))
    compileOnly("org.springframework:spring-core")
    compileOnly("net.bytebuddy:byte-buddy")
}
