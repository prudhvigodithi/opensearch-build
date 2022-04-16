package org.opensearch.gradle.zipplugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import java.util.*;
import java.lang.reflect.Field;
import java.io.File;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.Project;


public class ZipMavenCoordinatesTask extends DefaultTask {

	@TaskAction
	public void zipMavenCoordinates()  {
		System.out.println("Starting ZipMavenCoordinatesTask task");
		ZipPublishExtension ext =  getProject().getExtensions().findByType(ZipPublishExtension.class);
		if (ext == null) {
            ext = new ZipPublishExtension();
        }
		String zipGroup = ext.getZipgroup();
		String zipVersion = ext.getZipversion();
		String zipArtifact = ext.getZipartifact();
		String zipFilePath = ext.getZipfilePath();
		try {
			setEnv("zipVersion", zipVersion);
			setEnv("zipArtifact", zipArtifact);
			setEnv("zipFilePath", zipFilePath);
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
		System.out.println("zipVersion: passed is " + zipVersion);
		System.out.println("zipArtifact passed is " + zipArtifact);
		System.out.println("zipFilePath passed is " + zipFilePath);
		System.out.println("Successfully completed sample Task ZipMavenCoordinates");

    }
	@SuppressWarnings("unchecked")
	private static void setEnv(String key, String value) throws IllegalAccessException, NoSuchFieldException {
		Class<?>[] classes = Collections.class.getDeclaredClasses();
		Map<String, String> env = System.getenv();
		for (Class<?> cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
				Field field = cl.getDeclaredField("m");
				field.setAccessible(true);
				Object obj = field.get(env);
				Map<String, String> map = (Map<String, String>) obj;
				map.put(key, value);
			}
		}
	}

}

