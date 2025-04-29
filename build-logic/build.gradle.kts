plugins {
    `kotlin-dsl`
}
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

dependencies{
    implementation("tech.yanand.maven-central-publish:tech.yanand.maven-central-publish.gradle.plugin:1.3.0")
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


