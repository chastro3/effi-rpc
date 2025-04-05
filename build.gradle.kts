allprojects {
    apply(plugin = "base")
    group = "io.github.taikonaut3"
    version = "0.0.12-alpha"
    tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
    // resolve Gradle console Chinese character encoding issues
    tasks.withType<JavaExec> { systemProperties["sun.stdout.encoding"] = "utf-8" }
    tasks.named("clean") { doLast { delete(fileTree(projectDir).include("**/*.iml")) } }
}

subprojects {
    apply(plugin = "internal-maven-plugin")
    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            dependencies {
                add("compileOnly", "org.jetbrains:annotations")
                if (project.name != "effi-rpc-common")
                    add("annotationProcessor", project(":effi-rpc-common"))
            }
        }
    }
}



