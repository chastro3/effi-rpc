plugins {
    `kotlin-dsl`
}

description = "Gradle build logic and publishing conventions."

repositories {
    defaultRepositories()
}
gradlePlugin {
    plugins {
        create("internalMavenPublishPlugin") {
            id = "internal-maven-publish"
            implementationClass = "InternalMavenPublishPlugin"
        }
        create("internalModuleLoaderPlugin") {
            id = "internal-module-loader"
            implementationClass = "InternalModuleLoaderPlugin"
        }
    }
}

dependencies {
    implementation("com.gradleup.nmcp:nmcp:1.6.2")
}

fun RepositoryHandler.defaultRepositories() {
    mavenLocal()
    listOf(
        "https://maven.aliyun.com/repository/public/",
        "https://maven.aliyun.com/repository/jcenter/",
        "https://maven.aliyun.com/repository/google/",
        "https://maven.aliyun.com/repository/gradle-plugin/"
    ).forEach {
        maven { url = uri(it) }
    }
    mavenCentral()
    google()
    gradlePluginPortal()
}


