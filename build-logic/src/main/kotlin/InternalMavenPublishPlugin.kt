import groovy.namespace.QName
import groovy.util.Node
import nmcp.NmcpAggregationExtension
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
import java.io.File

class InternalMavenPublishPlugin : Plugin<Project> {

    private val supportedComponents = mapOf(
        "java" to "java",
        "java-platform" to "javaPlatform"
    )

    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("maven-publish")
            apply("signing")
            apply("com.gradleup.nmcp")
        }
        project.afterEvaluate {
            val component = findComponent(project) ?: return@afterEvaluate
            val publication = createPublication(project, component)
            configureSigning(project, publication)
            configureNmcp(project)
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
        val repositoryUrl = project.findProperty(PublishConfig.REPOSITORY_URL) as String?
            ?: error("maven.publish.repository.url must be set in gradle.properties (e.g. \"https://github.com/chastro3/effi-rpc\")")
        val licenseName = project.findProperty(PublishConfig.LICENSE_NAME) as String?
            ?: "The Apache Software License, Version 2.0"
        val licenseUrl = project.findProperty(PublishConfig.LICENSE_URL) as String?
            ?: "https://www.apache.org/licenses/LICENSE-2.0.txt"
        val developerName = project.findProperty(PublishConfig.DEVELOPER_NAME) as String?
            ?: error("maven.publish.developer.name must be set in gradle.properties")
        val developerEmail = project.findProperty(PublishConfig.DEVELOPER_EMAIL) as String?
            ?: error("maven.publish.developer.email must be set in gradle.properties")
        val scmUrl = project.findProperty(PublishConfig.SCM_URL) as String?
            ?: "scm:git:${repositoryUrl}.git"
        val path = project.path.replace(":", "/")
        publication.pom.apply {
            name.set(project.name)
            description.set(project.description ?: project.name)
            url.set("${repositoryUrl}/tree/master$path")
            licenses {
                license {
                    name.set(licenseName)
                    url.set(licenseUrl)
                }
            }
            developers {
                developer {
                    name.set(developerName)
                    email.set(developerEmail)
                }
            }
            scm {
                connection.set(scmUrl)
                developerConnection.set(scmUrl)
                url.set("${repositoryUrl}/tree/master$path")
            }
        }
    }

    private fun configureSigning(project: Project, publication: MavenPublication) {
        val keyId = project.findProperty(PublishConfig.SIGNING_KEY_ID) as? String ?: return
        val ringFile = project.findProperty(PublishConfig.SIGNING_SECRET_KEY_RING_FILE) as? String ?: return
        val password = project.findProperty(PublishConfig.SIGNING_PASSWORD) as? String ?: return
        project.extensions.configure(SigningExtension::class.java) {
            useInMemoryPgpKeys(keyId, File(ringFile).readText(), password)
            sign(publication)
        }
    }

    private fun configureNmcp(project: Project) {
        val username = project.findProperty(PublishConfig.CENTRAL_USERNAME) as? String ?: return
        val password = project.findProperty(PublishConfig.CENTRAL_PASSWORD) as? String ?: return
        project.rootProject.extensions.configure<NmcpAggregationExtension> {
            centralPortal {
                this.username.set(username)
                this.password.set(password)
                publishingType.set("AUTOMATIC")
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
                .firstOrNull { findNodeName(it) == "dependencyManagement" }
                ?.let { root.remove(it) }
        }
    }

    private fun findNodeName(node: Node): String {
        return when (val name = node.name()) {
            is QName -> name.localPart
            is String -> name
            else -> ""
        }
    }
}

