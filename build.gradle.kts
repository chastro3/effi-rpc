allprojects {
    apply(plugin = "base")
    group = "io.github.chastro3"
    version = "0.0.1-alpha"
    repositories {
        defaultRepositories()
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf(
                "-Agroup.id=${project.group}",
                "-Aartifact.id=${project.name}",
                "-Aversion=${project.version}",
                "-Anative.build=true"
            )
        )
    }
    // resolve Gradle console Chinese character encoding issues
    tasks.withType<JavaExec> { systemProperties["sun.stdout.encoding"] = "utf-8" }
    tasks.named("clean") { doLast { delete(fileTree(projectDir).include("**/*.iml")) } }
}

subprojects {
    if (isPublishEnabled()) apply(plugin = "internal-maven-publish")
    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            dependencies {
                add("compileOnly", "org.jetbrains:annotations")
                if (isProcessorEnabled()) add("annotationProcessor", project(":effi-rpc-processor"))
            }
        }
    }
}




