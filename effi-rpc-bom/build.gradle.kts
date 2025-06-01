plugins {
    id("java-platform")
}
description = "Manage dependency versions."

val jetbrainsVersion = "24.1.0"
val junitVersion = "5.10.0"
val vertxVersion = "4.5.14"
val nettyVersion = "4.1.121.Final"
val springBootVersion = "3.2.0"
val protobufBufVersion = "3.25.3"
val jclVersion = "1.3.4"
val nacosVersion = "2.3.0"
val asmVersion = "9.7.1"
val kryoVersion = "5.5.0"
val msgPackVersion = "0.9.8"
val disruptorVersion = "4.0.0"
val lz4Version = "1.8.0"
val snappyVersion = "1.1.10.5"
val consulVersion = "1.5.1"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.junit:junit-bom:$junitVersion"))
    api(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    api(platform("io.netty:netty-bom:$nettyVersion"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api(platform("com.google.protobuf:protobuf-bom:$protobufBufVersion"))

    constraints {
        api("org.jetbrains:annotations:$jetbrainsVersion")
        api("commons-logging:commons-logging:$jclVersion")
        api("org.ow2.asm:asm:$asmVersion")
        // https://mvnrepository.com/artifact/org.kiwiproject/consul-client
        api("org.kiwiproject:consul-client:$consulVersion")
        api("com.alibaba.nacos:nacos-client:$nacosVersion")
        api("com.esotericsoftware:kryo:$kryoVersion")
        api("org.msgpack:jackson-dataformat-msgpack:$msgPackVersion")
        api("com.lmax:disruptor:$disruptorVersion")
        api("org.lz4:lz4-java:$lz4Version")
        api("org.xerial.snappy:snappy-java:$snappyVersion")
        rootProject.subprojects.forEach({
            if (it.isPublishEnabled() && it.name != project.name)
                api("${it.group}:${it.name}:${it.version}")
        })
    }
}
