allprojects {
    apply(plugin = "base")
    group = "io.github.chastro3"
    version = "0.0.2-alpha"
    repositories {
        defaultRepositories()
    }
    configureTasks()
}

subprojects {
    if (publishEnabled()) apply(plugin = "internal-maven-publish")
    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            dependencies {
                add("compileOnly", "org.jetbrains:annotations")
                if (processorEnabled()) add("annotationProcessor", project(":effi-rpc-processor"))
            }
        }
    }
}




