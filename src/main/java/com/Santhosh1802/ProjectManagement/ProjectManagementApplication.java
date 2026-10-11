package com.Santhosh1802.ProjectManagement;

import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectManagementApplication {

	public static void main(String[] args) {
		ensureWritableTempDir();
		SpringApplication.run(ProjectManagementApplication.class, args);
	}

	private static void ensureWritableTempDir() {
		String tmp = System.getProperty("java.io.tmpdir");
		File tmpFile = tmp != null ? new File(tmp) : null;
		if (tmpFile == null || !tmpFile.canWrite() || tmp.toUpperCase().startsWith("C:\\WINDOWS")) {
			String localAppData = System.getenv("LOCALAPPDATA");
			File safeTemp = (localAppData != null && !localAppData.isBlank())
					? new File(localAppData, "Temp")
					: new File(System.getProperty("user.home", "."), ".pm-temp");

			if (!safeTemp.exists()) {
				safeTemp.mkdirs();
			}
			System.setProperty("java.io.tmpdir", safeTemp.getAbsolutePath());
		}
	}

}
