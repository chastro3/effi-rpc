import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
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

fun Project.configureTasks() {
    tasks.withType(JavaCompile::class.java).configureEach {
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

    tasks.withType(JavaExec::class.java).configureEach {
        systemProperties["sun.stdout.encoding"] = "utf-8"
    }

    tasks.withType(Javadoc::class.java).configureEach {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.named("clean").configure {
        doLast {
            delete(fileTree(projectDir).include("**/*.iml"))
        }
    }
}

fun Project.enablePublish(): Project {
    extra[Constants.PUBLISH] = true
    return this;
}

fun Project.enableProcessor(): Project {
    extra[Constants.PROCESSOR] = true
    return this
}

fun Project.publishEnabled(): Boolean {
    return hasTrueExtra(Constants.PUBLISH)
}

fun Project.processorEnabled(): Boolean {
    return hasTrueExtra(Constants.PROCESSOR)
}

fun Project.hasTrueExtra(key: String): Boolean {
    return extra.has(key) && extra.get(key) == true
}

object Constants {
    const val PUBLISH = "publish"
    const val PROCESSOR = "processor"
}

