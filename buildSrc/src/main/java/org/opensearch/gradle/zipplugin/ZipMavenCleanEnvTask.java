package org.opensearch.gradle.zipplugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import java.util.*;
import java.lang.reflect.Field;
import java.io.File;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.Project;


public class ZipMavenCleanEnvTask extends DefaultTask {

	@TaskAction
	public void zipMavenCleanEnv()  { 
		System.out.println("Running ZipMavenCleanEnvTask");
		clearEnv("zipArtifact", "zipVersion", "zipFilePath");
	}
	public static void clearEnv(String... keys) {
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			for (String key : keys) {
				env.remove(key);
			}
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
					.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			for (String key : keys) {
				cienv.remove(key);
			}
		} catch (NoSuchFieldException e) {
			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> env = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object obj = field.get(env);
						Map<String, String> map = (Map<String, String>) obj;
						for (String key : keys) {
							map.remove(key);
						}
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}

