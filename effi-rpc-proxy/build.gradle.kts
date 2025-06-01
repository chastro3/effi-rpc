plugins {
    id("java-library")
}
description  = "Proxy generation for interfaces and instance."
dependencies{
    api(project(":effi-rpc-config"))
    compileOnly("org.springframework:spring-core")
    compileOnly("net.bytebuddy:byte-buddy")
}