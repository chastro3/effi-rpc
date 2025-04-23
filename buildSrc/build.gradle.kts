plugins {
    `kotlin-dsl`
}
repositories {
    defaultRepositories()
}
rootProject.subprojects {
    repositories {
        defaultRepositories()
    }
}
gradlePlugin {
    plugins {
        create("internalMavenPlugin") {
            id = "internal-maven-plugin"
            implementationClass = "InternalMavenPlugin"
        }
    }
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


