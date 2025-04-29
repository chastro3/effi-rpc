import groovy.namespace.QName
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.signing.SigningExtension
import tech.yanand.gradle.mavenpublish.MavenCentralExtension
import java.io.File
import java.util.*

class InternalMavenPublishPlugin : Plugin<Project> {

    private val supportedComponents = mapOf(
        "java" to "java",
        "java-platform" to "javaPlatform"
    )

    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("maven-publish")
            apply("signing")
            apply("tech.yanand.maven-central-publish")
        }
        project.afterEvaluate {
            val component = findComponent(project) ?: return@afterEvaluate
            val publication = createPublication(project, component)
            configureSigning(project, publication)
            configureMavenCentral(project)
        }
    }

    private fun findComponent(project: Project): String? {
        return supportedComponents.entries
            .firstOrNull { project.pluginManager.hasPlugin(it.key) }
            ?.value
    }

    private fun createPublication(project: Project, component: String): MavenPublication {
        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        return publishing.publications.create("maven", MavenPublication::class.java).apply {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(project.components.getByName(component))
            if (component == "java") {
                addJavaArtifacts(project, this)
                applyVersionMapping(this)
            }
            configurePom(project, this)
        }
    }

    private fun addJavaArtifacts(project: Project, publication: MavenPublication) {
        val sourcesJar = project.tasks.register("sourcesJar", Jar::class.java) {
            archiveClassifier.set("sources")
            from(project.the<JavaPluginExtension>().sourceSets.getByName("main").allSource)
        }
        val javadocJar = project.tasks.register("javadocJar", Jar::class.java) {
            archiveClassifier.set("javadoc")
            dependsOn(project.tasks.named(JavaPlugin.JAVADOC_TASK_NAME))
            from(project.tasks.named(JavaPlugin.JAVADOC_TASK_NAME))
        }
        publication.artifact(sourcesJar.get())
        publication.artifact(javadocJar.get())
    }

    private fun configurePom(project: Project, publication: MavenPublication) {
        val path = project.path.replace(":", "/")
        publication.pom.apply {
            name.set(project.name)
            description.set(project.description)
            url.set("https://github.com/chastro3/effi-rpc/tree/master$path")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("chastro3")
                    email.set("wenbochou@163.com")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/chastro3/effi-rpc.git")
                developerConnection.set("scm:git:https://github.com/chastro3/effi-rpc.git")
                url.set("https://github.com/chastro3/effi-rpc/tree/master$path")
            }
        }
    }

    private fun configureSigning(project: Project, publication: MavenPublication) {
        project.extensions.configure(SigningExtension::class.java) {
            val keyId = project.findProperty("signing.keyId") as String?
            val ringFile = project.findProperty("signing.secretKeyRingFile") as String?
            val password = project.findProperty("signing.password") as String?
            if (!keyId.isNullOrBlank() && !ringFile.isNullOrBlank() && !password.isNullOrBlank()) {
                useInMemoryPgpKeys(keyId, File(ringFile).readText(), password)
                sign(publication)
            }
        }
    }

    private fun configureMavenCentral(project: Project) {
        project.extensions.configure<MavenCentralExtension> {
            val token = project.findProperty("maven.central.user.token") as String?
            if (!token.isNullOrBlank()) {
                val encodedToken = Base64.getEncoder().encodeToString(token.toByteArray())
                authToken.set(encodedToken)
                publishingType.set("AUTOMATIC")
                maxWait.set(60)
            }
        }
    }

    private fun applyVersionMapping(publication: MavenPublication) {
        publication.versionMapping {
            usage("java-api") { fromResolutionOf("runtimeClasspath") }
            usage("java-runtime") { fromResolutionResult() }
        }

        publication.pom.withXml {
            val root = asNode()
            root.children()
                .filterIsInstance<Node>()
                .firstOrNull { getNodeName(it) == "dependencyManagement" }
                ?.let { root.remove(it) }
        }
    }

    private fun getNodeName(node: Node): String {
        return when (val name = node.name()) {
            is QName -> name.localPart
            is String -> name
            else -> ""
        }
    }
}

