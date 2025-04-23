allprojects {
    apply(plugin = "base")
    group = "io.github.chastro3"
    version = "0.0.12-alpha"
    repositories {
        mavenLocal()
        listOf(
            "https://maven.aliyun.com/repository/public/",
            "https://maven.aliyun.com/repository/jcenter/",
            "https://maven.aliyun.com/repository/google/",
            "https://maven.aliyun.com/repository/gradle-plugin/"
        ).forEach { maven(it) }
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf("-Agroup.id=${project.group}", "-Aartifact.id=${project.name}", "-Aversion=${project.version}", "-Anative.build=true")
        )
    }
    // resolve Gradle console Chinese character encoding issues
    tasks.withType<JavaExec> { systemProperties["sun.stdout.encoding"] = "utf-8" }
    tasks.named("clean") { doLast { delete(fileTree(projectDir).include("**/*.iml")) } }
}

subprojects {
    val shouldPublish = project.extra.has("mavenPublish") &&
            (project.extra["mavenPublish"].toString().toBoolean())
    if(shouldPublish){
        apply(plugin = "maven-publish")

    }
    apply(plugin = "internal-maven-plugin")
    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            dependencies {
                add("compileOnly", "org.jetbrains:annotations")
                if (!listOf("effi-rpc-common", "effi-rpc-contract", "effi-rpc-processor").contains(project.name))
                    add("annotationProcessor", project(":effi-rpc-processor"))
            }
        }
    }
}




