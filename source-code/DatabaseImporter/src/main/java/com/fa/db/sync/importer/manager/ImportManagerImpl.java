package com.fa.db.sync.importer.manager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.fa.db.sync.importer.handler.FileHandler;
import com.fa.db.sync.importer.handler.ScriptHandler;

@Component
@Scope("singleton")
public class ImportManagerImpl implements ImportManager, Runnable {

	@Autowired
	ThreadPoolTaskScheduler taskScheduler;

	@Value("${sync.app.working.dir}")
	private String appWorkingDir;

	@Autowired
	ScriptHandler scriptHandler;

	@Autowired
	FileHandler fileHandler;

	public final String APP_DIR_NEW = "new";
	public final String APP_DIR_STAGING = "staging";
	public final String APP_DIR_SENT = "archieve";

	@PostConstruct
	public void initScanner() {
		taskScheduler.schedule(this,new CronTrigger("0 0/1 * * * ?"));

	}

	@Override
	public void run() {
		List<String> files = fileHandler.scanFiles();
		try {
			checkPreviousImportHasError();
			scriptHandler.processFileList(files);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println(new Date() + " -- Folder Scanned!");
		
	}

	public void checkPreviousImportHasError() {
		String destDirPath = appWorkingDir + File.separator + APP_DIR_STAGING;
		File destDir = new File(destDirPath);
		File [] destDirFileList = destDir.listFiles();
		if(destDirFileList.length>0)
		{
			System.out.println("\n\nSYNC : DB IMPORTER ERROR!");
			System.out.println("SYNC : ERROR : There are unfinished files in " +appWorkingDir + File.separator + APP_DIR_STAGING );
			System.out.println("SYNC : ERROR : See previous error logs. After solving the problem move all zip files to  " + appWorkingDir + File.separator + APP_DIR_NEW + " and remove any other files and folders.");
			System.out.println("SYNC : ERROR : Restart Importer!\n\n");
					
			System.exit(0);
		}
	}
}
