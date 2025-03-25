import groovy.util.Node;
import groovy.util.NodeList;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import java.util.Map;

/**
 * Internal maven publish plugin.
 */
public class InternalMavenPlugin implements Plugin<Project> {

    private static final Map<String, String> SUPPORT_PLUGINS = Map.of(
            "java", "java",
            "java-platform", "javaPlatform"
    );

    @Override
    public void apply(Project project) {
        PluginManager pluginManager = project.getPluginManager();
        project.afterEvaluate(item -> {
            String supportedComponent = getSupportComponent(pluginManager);
            if (supportedComponent != null) {
                pluginManager.apply(MavenPublishPlugin.class);
                MavenPublication mavenPublication = project.getExtensions()
                        .getByType(PublishingExtension.class).getPublications()
                        .create("maven", MavenPublication.class);
                mavenPublication.setGroupId(project.getGroup().toString());
                mavenPublication.setArtifactId(project.getName());
                mavenPublication.setVersion(project.getVersion().toString());
                mavenPublication.from(project.getComponents().getByName(supportedComponent));

                String path = project.getPath().replace(":", "/");

                MavenPom pom = mavenPublication.getPom();
                pom.getName().set(project.getName());
                pom.getDescription().set(project.getDescription());
                pom.getUrl().set("https://github.com/taikonaut3/effi-rpc/tree/master" + path);

                pom.licenses(licenses -> {
                    licenses.license(license -> {
                        license.getName().set("The Apache Software License, Version 2.0");
                        license.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0.txt");
                    });
                });

                pom.developers(developers -> {
                    developers.developer(developer -> {
                        developer.getName().set("taikonaut3");
                        developer.getEmail().set("wenbochou@163.com");
                    });
                });

                pom.scm(scm -> {
                    scm.getConnection().set("scm:git:https://github.com/taikonaut3/effi-rpc.git");
                    scm.getDeveloperConnection().set("scm:git:https://github.com/taikonaut3/effi-rpc.git");
                    scm.getUrl().set(pom.getUrl());
                });

                if (supportedComponent.equals("javaPlatform")) {
                    project.getGradle().projectsEvaluated(g -> addProjectDependencyManagement(pom, project));
                }
            }

        });

    }

    public void addProjectDependencyManagement(MavenPom pom, Project project) {
        for (Project subproject : project.getRootProject().getSubprojects()) {
            if (subproject.getPluginManager().hasPlugin("java")) {
                pom.withXml(xml -> {
                    Node xmlNode = xml.asNode();
                    Node dependencyManagement = findPomNode(xmlNode, "dependencyManagement");
                    Node dependencies = findPomNode(dependencyManagement, "dependencies");
                    Node dependency = dependencies.appendNode("dependency");
                    dependency.appendNode("groupId", subproject.getGroup());
                    dependency.appendNode("artifactId", subproject.getName());
                    dependency.appendNode("version", subproject.getVersion());
                });
            }
        }
    }

    private Node findPomNode(Node node, String childName) {
        NodeList nodeList = (NodeList) node.get(childName);
        if (nodeList != null && !nodeList.isEmpty()) {
            return (Node) nodeList.getFirst();
        }
        return null;
    }

    private String getSupportComponent(PluginManager pluginManager) {
        for (String plugin : SUPPORT_PLUGINS.keySet()) {
            if (pluginManager.hasPlugin(plugin)) {
                return SUPPORT_PLUGINS.get(plugin);
            }
        }
        return null;
    }

}
