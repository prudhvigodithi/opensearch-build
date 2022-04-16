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
import org.opensearch.gradle.zipplugin.ZipMavenCleanEnvTask;
import org.opensearch.gradle.zipplugin.ZipMavenCoordinatesTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.Task;


public class ZipPublish implements Plugin<Project> {

    static final String EXTENSION_NAME = "zipmavensettings";
    public final static String PUBLICATION_NAME = "mavenZip";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(EXTENSION_NAME, ZipPublishExtension.class);

        ZipMavenCoordinatesTask starttask = project.getTasks().create("ZipMavenCoordinatesTask", ZipMavenCoordinatesTask.class);
        ZipMavenCleanEnvTask endtask =  project.getTasks().create("ZipMavenCleanEnv", ZipMavenCleanEnvTask.class);

        final Path buildDirectory = project.getRootDir().toPath();
        project.getPluginManager().apply(MavenPublishPlugin.class);

        project.getExtensions().configure(PublishingExtension.class, publishing -> {
            publishing.repositories(repositories -> {
                repositories.maven(maven -> {
                    maven.setName("staging");
                    maven.setUrl(buildDirectory.toString() + "/local-staging-repo");
                });
            });
            System.out.println("Starting publishMavenZipPublicationToStagingRepository task");
            publishing.publications(publications -> {
                publications.create(PUBLICATION_NAME, MavenPublication.class, mavenZip -> {
                    ZipPublishExtension extset = project.getExtensions().findByType(ZipPublishExtension.class);
                    String zipGroup = extset.getZipgroup();
                    String zipFilePath = System.getenv("zipFilePath");
                    System.out.println("The zip artifact location is "+ zipFilePath);
                    String zipArtifact = System.getenv("zipArtifact"); 
                    System.out.println("The entered maven ArtifactId " + zipArtifact);
                    String zipVersion = System.getenv("zipVersion");
                    System.out.println("The entered maven Version is " + zipVersion);
                    System.out.println("Setting the maven coordinates, will now execute ZipMavenCoordinatesTask task");
                    mavenZip.artifact(buildDirectory.toString() + zipFilePath);
                    mavenZip.setGroupId(zipGroup);
                    mavenZip.setArtifactId(zipArtifact);
                    mavenZip.setVersion(zipVersion);
                    mavenZip.pom(pom -> {
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
                    });
                });
            });
        });
        project.getTasks().getByName("publishMavenZipPublicationToStagingRepository").dependsOn("ZipMavenCoordinatesTask");
    }
}