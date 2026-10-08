plugins {
    id("java-platform")
}
description = "Dependency version alignment for Effi RPC."

val jspecifyVersion = "1.0.1"
val jetbrainsVersion = "26.1.0"
val junitVersion = "6.1.3"
val vertxVersion = "5.2.0"
val nettyVersion = "4.2.18.Final"
val springBootVersion = "4.1.1"
val protobufBufVersion = "4.36.2"
val jclVersion = "1.4.0"
val nacosVersion = "3.2.4"
val asmVersion = "9.10.1"
val kryoVersion = "5.6.2"
val disruptorVersion = "4.0.0"
val lz4Version = "1.8.1"
val snappyVersion = "1.1.10.8"
val consulVersion = "1.12.1"
val jctoolsVersion = "4.0.7"
val autoCommonVersion = "1.2.2"
val jmhVersion = "1.37"

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
        api("org.jspecify:jspecify:$jspecifyVersion")
        api("org.jetbrains:annotations:$jetbrainsVersion")
        api("commons-logging:commons-logging:$jclVersion")
        api("org.ow2.asm:asm:$asmVersion")
        // https://mvnrepository.com/artifact/org.kiwiproject/consul-client
        api("org.kiwiproject:consul-client:$consulVersion")
        api("com.alibaba.nacos:nacos-client:$nacosVersion")
        api("com.esotericsoftware:kryo:$kryoVersion")
        api("com.lmax:disruptor:$disruptorVersion")
        api("at.yawk.lz4:lz4-java:$lz4Version")
        api("org.xerial.snappy:snappy-java:$snappyVersion")
        api("org.jctools:jctools-core:$jctoolsVersion")
        api("com.google.auto:auto-common:$autoCommonVersion")
        api("org.openjdk.jmh:jmh-core:$jmhVersion")
        api("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
        rootProject.subprojects.forEach({
            if (it.publishEnabled() && it.name != project.name)
                api("${it.group}:${it.name}:${it.version}")
        })
    }
}
