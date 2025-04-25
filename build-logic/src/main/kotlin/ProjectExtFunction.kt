import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.extra
import java.net.URI

fun RepositoryHandler.defaultRepositories() {
    mavenLocal()
    listOf(
        "https://maven.aliyun.com/repository/public/",
        "https://maven.aliyun.com/repository/jcenter/",
        "https://maven.aliyun.com/repository/google/",
        "https://maven.aliyun.com/repository/gradle-plugin/"
    ).forEach {
        maven { url = URI(it) }
    }
    mavenCentral()
    google()
    gradlePluginPortal()
}

fun Project.enablePublish(): Project {
    extra[Constants.PUBLISH] = true
    return this;
}

fun Project.enableProcessor(): Project {
    extra[Constants.PROCESSOR] = true
    return this
}

fun Project.isPublishEnabled(): Boolean {
    return hasTrueExtra(Constants.PUBLISH)
}

fun Project.isProcessorEnabled(): Boolean {
    return hasTrueExtra(Constants.PROCESSOR)
}

fun Project.hasTrueExtra(key: String): Boolean {
    return extra.has(key) && extra.get(key) == true
}

object Constants {
    const val PUBLISH = "publish"
    const val PROCESSOR = "processor"
}

