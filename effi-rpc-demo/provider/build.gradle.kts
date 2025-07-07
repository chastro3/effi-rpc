dependencies {
    implementation(project(":effi-rpc-protocols:effi-rpc-http"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation(project(":effi-rpc-registry:effi-rpc-registry-consul"))
    implementation(project(":effi-rpc-registry:effi-rpc-registry-nacos"))
    implementation(project(":effi-rpc-marshalling"))
    implementation("org.slf4j:slf4j-api")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation(project(":effi-rpc-integration:effi-rpc-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

plugins {
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("application")
}

application {
    mainClass.set("demo.provider.Provider")
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

//tasks.register<Jar>("fatJar") {
//    group = "build"
//    archiveBaseName.set("provider")
//    archiveVersion.set("1.0")
//    destinationDirectory.set(file("$buildDir/libs"))
//
//    // 添加主代码（编译后的 class 文件）
//    from(sourceSets.main.get().output)
//
//    // 设置 lib 目录，并将依赖 JAR 文件拷贝到 lib 目录下
//    dependsOn(configurations.runtimeClasspath)
//    from({
//        configurations.runtimeClasspath.get()
//            .filter { it.name.endsWith("jar") } // 只包括 JAR 文件
//    }) {
//        // 设置目标路径为 lib 目录
//        into("lib")
//    }
//
//    // 设置清晰的入口类
//    manifest {
//        attributes(
//            "Manifest-Version" to "1.0", // Manifest 版本
//            "Main-Class" to "demo.provider.FatJarTest" // 入口类
//        )
//    }
//}
























