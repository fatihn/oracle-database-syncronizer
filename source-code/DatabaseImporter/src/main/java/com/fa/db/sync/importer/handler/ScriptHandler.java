package com.fa.db.sync.importer.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import com.fa.db.sync.importer.dao.GeneralDao;
import com.fa.db.sync.importer.util.ZipUtil;

@Component
public class ScriptHandler {
	@Value("${sync.app.working.dir}")
	private String appWorkingDir;

	@Autowired
	GeneralDao generalDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String APP_DIR_NEW = "new";
	public String APP_DIR_STAGING = "staging";
	public String APP_DIR_ARCHIEVE = "archieve";

	public void processFileList(List<String> files) throws IOException, SQLException {
		for (String fileName : files) {
			System.out.println(fileName);
			processFile(fileName);
		}
	}
	
	public void processFile(String zipFileName) throws IOException {
		System.out.println("SYNC : Import started : File : " + zipFileName);

		File zipFile = new File(appWorkingDir + File.separator + APP_DIR_NEW + File.separator + zipFileName);
		String zipToBeMovedPath = appWorkingDir + File.separator + APP_DIR_STAGING + File.separator + zipFileName;

		zipFile.renameTo(new File(zipToBeMovedPath));
		String destDirPath = appWorkingDir + File.separator + APP_DIR_STAGING;
		
		ZipUtil.unzip(zipToBeMovedPath, destDirPath);

		String sqlFilePath = appWorkingDir + File.separator + APP_DIR_STAGING + File.separator
				+ zipFileName.substring(0, zipFileName.length() - 4) + File.separator
				+ zipFileName.substring(0, zipFileName.length() - 4) + ".sql";
		System.out.println(sqlFilePath);

		Connection conn = null;
		try {
			conn = jdbcTemplate.getDataSource().getConnection();
			conn.setAutoCommit(false);
			String content = new String(Files.readAllBytes(Paths.get(sqlFilePath)));
			if(content.trim().equals(""))
				System.out.println("File is empty nothing to execute!");
			else {
				ScriptUtils.executeSqlScript(conn, new FileSystemResource(sqlFilePath));
				System.out.println("File content executed successfully!");
				
			}
			System.out.println("SYNC : Script file executed : File : " + sqlFilePath);

			List<String> lobFileList = new ArrayList<String>();
			File zipExtractedFolder = new File(
					destDirPath + File.separator + zipFileName.substring(0, zipFileName.length() - 4));
			File[] dirFiles = zipExtractedFolder.listFiles();
			if (dirFiles != null) {
				for (File file : dirFiles) {
					String fn = file.getName();
					if (fn.endsWith(".lob")) {
						lobFileList.add(fn);
					}
				}
			}

			for (String lobFileName : lobFileList) {
				System.out.println("Lob File : " + zipExtractedFolder.getAbsolutePath() + File.separator + lobFileName);
				generalDao.processLobFile(conn, lobFileName,
						zipExtractedFolder.getAbsolutePath() + File.separator + lobFileName);

			}
			System.out.println("SYNC : Lob files imported");

			FileUtils.deleteDirectory(zipExtractedFolder);
			new File(zipToBeMovedPath).renameTo(
					new File(appWorkingDir + File.separator + APP_DIR_ARCHIEVE + File.separator + zipFileName));
			FileUtils.forceDelete(
					new File(appWorkingDir + File.separator + APP_DIR_NEW + File.separator + zipFileName + ".status"));
			System.out.println("SYNC : Import Completed");

			conn.commit();

		} catch (SQLException ex) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			ex.printStackTrace();
		}finally {
			if (conn != null)
				JdbcUtils.closeConnection(conn);
		}
	}
}
