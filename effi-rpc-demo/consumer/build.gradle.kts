dependencies {
    implementation(project(":effi-rpc-demo:api"))
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    implementation(project(":effi-rpc-marshalling"))
    implementation(project(":effi-rpc-proxy"))
    implementation("org.slf4j:slf4j-api")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic")
}
plugins {
    id("org.graalvm.buildtools.native") version "1.1.14"
    id("application")
}

application {
    mainClass.set("demo.consumer.Consumer")
//    applicationDefaultJvmArgs = listOf(
//        "-agentlib:native-image-agent=config-output-dir=${buildDir}/native-image,config-write-period-secs=60,config-write-initial-delay-secs=5"
//    )
}

tasks.register<JavaExec>("runInterfaceConsumer") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("demo.consumer.InterfaceConsumer")
}

graalvmNative {
    binaries.all {
        // common options
        verbose.set(true)
        sharedLibrary.set(false)
    }
}
