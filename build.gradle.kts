plugins {
    base
}

description = "Root build for the Effi RPC multi-module project."

allprojects {
    group = "cc.uniplat"
    version = "0.0.2-alpha"
    repositories {
        defaultRepositories()
    }
    configureTasks()
}

subprojects {
    if (publishEnabled()) {
        pluginManager.apply("internal-maven-publish")
    }
    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            dependencies {
                add("compileOnly", "org.jetbrains:annotations")
                if (processorEnabled()) add("annotationProcessor", project(":effi-rpc-processor"))
            }
        }
    }
}




