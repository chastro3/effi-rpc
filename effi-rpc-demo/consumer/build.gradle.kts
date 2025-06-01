dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    implementation(project(":effi-rpc-marshalling"))
    implementation(project(":effi-rpc-proxy"))
    implementation("org.slf4j:slf4j-api")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.16")
}
plugins {
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("application")
}

application {
    mainClass.set("demo.consumer.Consumer")
//    applicationDefaultJvmArgs = listOf(
//        "-agentlib:native-image-agent=config-output-dir=${buildDir}/native-image,config-write-period-secs=60,config-write-initial-delay-secs=5"
//    )
}

graalvmNative {
    binaries.all {
        // common options
        verbose.set(true)
        sharedLibrary.set(false)
    }
}
