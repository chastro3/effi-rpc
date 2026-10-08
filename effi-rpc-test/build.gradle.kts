plugins {
    id("java-library")
}

description = "Integration tests and benchmarks."

dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    testImplementation(project(":effi-rpc-integration:effi-rpc-spring-boot-starter"))
    implementation(project(":effi-rpc-proxy"))
    implementation("tools.jackson.core:jackson-databind")
    implementation("com.esotericsoftware:kryo")
    implementation("com.google.protobuf:protobuf-java")
    implementation("com.google.protobuf:protobuf-java-util")
    annotationProcessor(project(":effi-rpc-processor"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.glassfish.jersey.core:jersey-server")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.google.auto:auto-common")
    implementation("com.lmax:disruptor")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    implementation("org.openjdk.jmh:jmh-core")
    annotationProcessor(platform(project(":effi-rpc-bom")))
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")
    testAnnotationProcessor(project(":effi-rpc-processor"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    runtimeOnly("org.springframework:spring-core")
    runtimeOnly("net.bytebuddy:byte-buddy")
    testImplementation("at.yawk.lz4:lz4-java")
    testImplementation("org.xerial.snappy:snappy-java")
}

tasks.test {
    enabled = true
    useJUnitPlatform()
}

tasks.register<JavaExec>("mpscEventBusBenchmark") {
    group = "benchmark"
    description = "Runs the MpscEventBus JMH benchmark."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.effi.rpc.benchmark.MpscEventBusBenchmark")
}

tasks.register<JavaExec>("disruptorEventDispatcherBenchmark") {
    group = "benchmark"
    description = "Runs the Disruptor EventDispatcher JMH benchmark."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.effi.rpc.benchmark.DisruptorEventDispatcherBenchmark")
}

tasks.register<JavaExec>("mpscEventBusNoMetricsBenchmark") {
    group = "benchmark"
    description = "Runs the MpscEventBus no-metrics JMH benchmark."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.effi.rpc.benchmark.MpscEventBusNoMetricsBenchmark")
}

tasks.register<JavaExec>("mpscEventBusShardedBenchmark") {
    group = "benchmark"
    description = "Runs the sharded telemetry MpscEventBus JMH benchmark."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.effi.rpc.benchmark.MpscEventBusShardedBenchmark")
}

tasks.register<JavaExec>("proxyInvocationBenchmark") {
    group = "benchmark"
    description = "Runs the proxy invocation JMH benchmark."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.effi.rpc.benchmark.ProxyInvocationBenchmark")
}
