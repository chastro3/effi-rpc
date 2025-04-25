import groovy.namespace.QName
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class InternalMavenPublishPlugin : Plugin<Project> {

    private val supportPlugins = mapOf(
        "java" to "java",
        "java-platform" to "javaPlatform"
    )

    override fun apply(project: Project) {
        val pluginManager = project.pluginManager

        project.afterEvaluate {
            val supportedComponent = getSupportedComponent(pluginManager)
            if (supportedComponent != null) {
                pluginManager.apply(MavenPublishPlugin::class.java)

                val publishing = project.extensions.getByType(PublishingExtension::class.java)
                val publication = publishing.publications.create("maven", MavenPublication::class.java)

                publication.groupId = project.group.toString()
                publication.artifactId = project.name
                publication.version = project.version.toString()
                publication.from(project.components.getByName(supportedComponent))

                applyVersionMapping(publication, supportedComponent)

                val path = project.path.replace(":", "/")

                with(publication.pom) {
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
        }
    }

    private fun getSupportedComponent(pluginManager: PluginManager): String? {
        return supportPlugins.entries.firstOrNull { pluginManager.hasPlugin(it.key) }?.value
    }


    private fun applyVersionMapping(publication: MavenPublication, supportedComponent: String) {
        if (supportedComponent != "java") return

        publication.versionMapping {
            usage("java-api") {
                fromResolutionOf("runtimeClasspath")
            }
            usage("java-runtime") {
                fromResolutionResult()
            }
        }

        publication.pom.withXml {
            val pomNode = asNode()
            val dependencyManagementNode = pomNode.children().find {
                it is Node && when (val name = it.name()) {
                    is QName -> name.localPart == "dependencyManagement"
                    is String -> name == "dependencyManagement"
                    else -> false
                }
            } as? Node

            if (dependencyManagementNode != null) {
                pomNode.remove(dependencyManagementNode)
            }
        }
    }

}
