package org.opensearch.gradle.zipplugin;

import java.util.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import java.nio.file.Path;
import org.opensearch.gradle.zipplugin.ZipPublishExtension;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.Task;


public class ZipPublish implements Plugin<Project> {
    private Project project;

    static final String EXTENSION_NAME = "zipmavensettings";
    public final static String PUBLICATION_NAME = "mavenzip";

    private void configMaven() {
        this.project.getPluginManager().apply(MavenPublishPlugin.class);
        final Path buildDirectory = this.project.getRootDir().toPath();
        this.project.getExtensions().configure(PublishingExtension.class, publishing -> {
            publishing.repositories(repositories -> {
                repositories.maven(maven -> {
                    maven.setName("zipstaging");
                    maven.setUrl(buildDirectory.toString() + "/build/local-staging-repo");
                });
            });
            System.out.println("Starting publishMavenZipPublicationToZipstagingRepository task");
            publishing.publications(publications -> {
                publications.create(PUBLICATION_NAME, MavenPublication.class, mavenZip -> {
                    ZipPublishExtension extset = this.project.getExtensions().findByType(ZipPublishExtension.class);
                    String zipGroup = extset.getZipgroup();
                    String zipArtifact = getProperty("zipArtifact");
                    String zipFilePath = getProperty("zipFilePath");
                    System.out.println("The zip artifact location is "+ zipFilePath);
                    System.out.println("The entered maven ArtifactId " + zipArtifact);
                    String zipVersion = getProperty("zipVersion");
                    System.out.println("The entered maven Version is " + zipVersion);
                    mavenZip.artifact(buildDirectory.toString() + zipFilePath);
                    mavenZip.setGroupId(zipGroup);
                    mavenZip.setArtifactId(zipArtifact);
                    mavenZip.setVersion(zipVersion);
                    /*mavenZip.pom(pom -> {
                        pom.licenses(licenses -> {
                            licenses.license(license -> {
                                license.getName().set("The Apache License, Version 2.0");
                                license.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
                            });
                        });
                        pom.developers(developers -> {
                            developers.developer(developer -> {
                                developer.getId().set("OpenSearch");
                                developer.getName().set("zipArtifact");
                                developer.getOrganizationUrl().set("https://github.com/opensearch-project/job-scheduler");
                            });
                        });
                    });*/
                });
            });
        });
    }
    private String getProperty(String name) {
        if (this.project.hasProperty(name)) {
            Object property = this.project.property(name);
            if (property != null) {
                return property.toString();
            }
        }
        return null;
    }

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getExtensions().create(EXTENSION_NAME, ZipPublishExtension.class);
        configMaven();
        //project.getTasks().getByName("generatePomFileForMavenzipPublication").dependsOn("publishMavenZipPublicationToZipstagingRepository");
    }
}
