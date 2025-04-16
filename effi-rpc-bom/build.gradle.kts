plugins {
    id("java-platform")
}
description = "Management dependency versions."

val junitVersion = "5.10.0"
val jetbrainsVersion = "24.1.0"
val vertxVersion = "4.5.9"
val asmVersion = "9.7.1"
val slf4jVersion = "2.0.9"
val log4j2Version = "2.23.1"
val jclVersion = "1.3.4"
val nacosVersion = "2.3.0"
val springBootVersion = "3.2.0"
val bytebuddyVersion = "1.14.11"
val jacksonVersion = "2.15.3"
val kryoVersion = "5.5.0"
val msgPackVersion = "0.9.8"
val protobufBufVersion = "3.25.3"
val disruptorVersion = "4.0.0"
val lz4Version = "1.8.0"
val snappyVersion = "1.1.10.5"
val jakartaWsRsVersion = "3.1.0"
val nettyVersion = "4.1.119.Final"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.jetbrains:annotations:${jetbrainsVersion}"))
    api(platform("org.junit:junit-bom:${junitVersion}"))
    api(platform("io.vertx:vertx-dependencies:${vertxVersion}"))
    api(platform("io.netty:netty-bom:${nettyVersion}"))
    api(platform("org.ow2.asm:asm:${asmVersion}"))
    api(platform("org.slf4j:slf4j-api:${slf4jVersion}"))
    api(platform("org.apache.logging.log4j:log4j-api:${log4j2Version}"))
    api(platform("commons-logging:commons-logging:${jclVersion}"))
    api(platform("com.alibaba.nacos:nacos-client:${nacosVersion}"))
    api(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    api(platform("net.bytebuddy:byte-buddy:${bytebuddyVersion}"))
    api(platform("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}"))
    api(platform("com.esotericsoftware:kryo:${kryoVersion}"))
    api(platform("org.msgpack:jackson-dataformat-msgpack:${msgPackVersion}"))
    api(platform("com.google.protobuf:protobuf-bom:${protobufBufVersion}"))
    api(platform("com.lmax:disruptor:${disruptorVersion}"))
    api(platform("org.lz4:lz4-java:${lz4Version}"))
    api(platform("org.xerial.snappy:snappy-java:${snappyVersion}"))
    api(platform("jakarta.ws.rs:jakarta.ws.rs-api:${jakartaWsRsVersion}"))
}