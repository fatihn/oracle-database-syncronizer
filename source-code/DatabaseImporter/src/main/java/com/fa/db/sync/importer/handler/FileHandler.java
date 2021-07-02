package com.fa.db.sync.importer.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileHandler {
	@Value("${sync.app.working.dir}")
	private String appWorkingDir;

	public String APP_DIR_NEW = "new";

	public List<String> scanFiles() {
		List<String> filesToBeProcessed = new ArrayList<String>();

		File dir = new File(appWorkingDir + File.separator + APP_DIR_NEW);

		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException(
					"App directory does not exist! -- " + appWorkingDir + File.separator + APP_DIR_NEW);

		List<String> dirFileList = new ArrayList<String>();

		File[] fileList = dir.listFiles();
		if (fileList != null) {
			for (File file : fileList) {
				String fileName = file.getName();
				if (fileName.endsWith(".zip") || fileName.endsWith(".status")) {
					dirFileList.add(fileName);
				}
			}
		}
		List<String> statusFileList = new ArrayList<String>();
		for (String dirFile : dirFileList) {
			if (dirFile.endsWith(".status"))
				statusFileList.add(dirFile);

		}

		for (String statusFileName : statusFileList) {
			for (String fileName : dirFileList) {
				if (statusFileName.equals(fileName + ".status")) {
					filesToBeProcessed.add(fileName);
				}
			}
		}
		Collections.sort(filesToBeProcessed);
		
		return filesToBeProcessed;
	}
}
