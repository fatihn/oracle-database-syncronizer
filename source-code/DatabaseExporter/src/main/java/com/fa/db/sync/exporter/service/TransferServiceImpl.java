package com.fa.db.sync.exporter.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fa.db.sync.exporter.dao.TableColumnDao;
import com.fa.db.sync.exporter.dao.TableSequenceDao;
import com.fa.db.sync.exporter.dao.TransferDao;
import com.fa.db.sync.exporter.dto.TableColumn;
import com.fa.db.sync.exporter.dto.Transfer;
import com.fa.db.sync.exporter.util.ZipUtil;

@Service
public class TransferServiceImpl implements TransferService {
	@Autowired
	private TransferDao transferDao;

	@Autowired
	private TableColumnDao tableColumnDao;

	@Autowired
	private TableSequenceDao tableSequenceDao;

	@Value("${sync.ssh.ip}")
	private String targetSshIp;
	@Value("${sync.ssh.user}")
	private String targetSshUser;
	@Value("${sync.ssh.password}")
	private String targetSshPassword;
	@Value("${sync.ssh.target.folder}")
	private String targetSshFolder;
	@Value("${sync.app.working.dir}")
	private String appWorkingDir;

	Map<String, List<TableColumn>> tableDefinitionMap = new HashMap<String, List<TableColumn>>();

	public final String APP_DIR_NEW = "new";
	public final String APP_DIR_SENT = "sent";

	@Override
	public List<Transfer> processPendingTransfers() throws SQLException, IOException, InterruptedException {
		System.out.println("\n-------\n");
		System.out.println("SYNC : EXPORT STARTED!");
		transferDao.markExportedStatusAsPending();
		clearFoldersInPreviousExport();
		Map<String, String> tableSequenceMap = tableSequenceDao.getTableSequences();

		List<Transfer> pendingTransferList = transferDao.getPendingTransfers();
		if (pendingTransferList.size() > 0) {

			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fileName = df.format(Calendar.getInstance().getTime());

			String dumpDirectoryPathInNew = appWorkingDir + File.separator + APP_DIR_NEW + File.separator + fileName
					+ File.separator;
			File dir = new File(dumpDirectoryPathInNew);
			dir.mkdirs();

			File exportSqlFile = new File(dumpDirectoryPathInNew + File.separator + fileName + ".sql");
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(exportSqlFile, true));
			List<Long> transferIdList = new ArrayList<Long>();
			List<Long> transferIdSubList = new ArrayList<Long>();

			System.out.println("SYNC : Exporting to directory : [ " + dumpDirectoryPathInNew + " ]");
			int i = 0;
			int page = 1;

			while (!pendingTransferList.isEmpty()) {
				for (Transfer transfer : pendingTransferList) {
					i++;
					transferIdList.add(transfer.getId());
					transferIdSubList.add(transfer.getId());

					List<TableColumn> tableColumnList = tableDefinitionMap.get(transfer.getTableName());
					if (tableColumnList == null) {
						List<TableColumn> tcList = tableColumnDao.getTableColumns(transfer.getTableOwner(),
								transfer.getTableName());
						tableDefinitionMap.put(transfer.getTableName(), tcList);
						tableColumnList = tcList;
					}

					if (transfer.getOpType().equals("I")) {

						String insertStmt = "insert into " + transfer.getTableOwner() + "." + transfer.getTableName()
								+ "(";
						String selectStmt = "select ";
						for (TableColumn tc : tableColumnList) {
							if (tc.getDataType().equals("DATE")) {
								selectStmt += "'<' || to_char(" + tc.getColumnName()
										+ ", 'YYYY-MM-DD HH24:MI:SS') || '>',";
								insertStmt += tc.getColumnName() + ",";
							} else if (tc.getDataType().equals("BLOB") || tc.getDataType().equals("CLOB")
									|| tc.getDataType().equals("NCLOB")) {
								tableColumnDao.readLobToFile(transfer, tc, dumpDirectoryPathInNew);
							} else {
								selectStmt += tc.getColumnName() + ",";
								insertStmt += tc.getColumnName() + ",";
							}
						}
						selectStmt = selectStmt.substring(0, selectStmt.length() - 1);
						insertStmt = insertStmt.substring(0, insertStmt.length() - 1);

						selectStmt += " from " + transfer.getTableOwner() + "." + transfer.getTableName() + " where "
								+ transfer.getUniqueKeyStatement();

						String insertStmtValues = transferDao.getInsertStmtValues(selectStmt);

						if (!insertStmtValues.equals("") || insertStmtValues.length() > 0) {
							insertStmtValues = insertStmtValues.replace("'<", "<");
							insertStmtValues = insertStmtValues.replace(">'", ">");
							insertStmtValues = insertStmtValues.replaceAll("<", "to_date('");
							insertStmtValues = insertStmtValues.replaceAll(">", "','YYYY-MM-DD HH24:MI:SS')");
							insertStmtValues = insertStmtValues.replace("to_date('','YYYY-MM-DD HH24:MI:SS')", "null");

							insertStmt += ") values (" + insertStmtValues + ")";

							fileWriter.write(insertStmt);
							fileWriter.newLine();
							if (tableSequenceMap.containsKey(transfer.getTableName())) {
								String seqStr = "select ecsms." + tableSequenceMap.get(transfer.getTableName())
										+ ".nextval from dual";
								fileWriter.write(seqStr);
								fileWriter.newLine();
							}
						}
					} else if (transfer.getOpType().equals("D")) {
						String deleteStmt = "delete from " + transfer.getTableOwner() + "." + transfer.getTableName()
								+ " where " + transfer.getUniqueKeyStatement();
						fileWriter.write(deleteStmt);
						fileWriter.newLine();
					} else if (transfer.getOpType().equals("U")) {
						String updateStmt = "update " + transfer.getTableOwner() + "." + transfer.getTableName()
								+ " set ";

						String selectStmt = "select ";
						for (TableColumn tc : tableColumnList) {
							if (tc.getDataType().equals("DATE")) {
								selectStmt += "'<' || to_char(" + tc.getColumnName()
										+ ", 'YYYY-MM-DD HH24:MI:SS') || '>'";
							} else if (tc.getDataType().equals("BLOB") || tc.getDataType().equals("CLOB")
									|| tc.getDataType().equals("NCLOB")) {
								tableColumnDao.readLobToFile(transfer, tc, dumpDirectoryPathInNew);
							} else {
								selectStmt += tc.getColumnName() + "";
							}
							selectStmt += " as " + tc.getColumnName()+",";
						}
						selectStmt = selectStmt.substring(0, selectStmt.length() - 1);
						selectStmt += " from " + transfer.getTableOwner() + "." + transfer.getTableName() + " where "
								+ transfer.getUniqueKeyStatement();
						
						String updateStmtValues = transferDao.getUpdateStmtValues(selectStmt);
												
						updateStmtValues = updateStmtValues.replace("'<", "<");
						updateStmtValues = updateStmtValues.replace(">'", ">");
						updateStmtValues = updateStmtValues.replaceAll("<", "to_date('");
						updateStmtValues = updateStmtValues.replaceAll(">", "','YYYY-MM-DD HH24:MI:SS')");
						updateStmtValues = updateStmtValues.replace("to_date('','YYYY-MM-DD HH24:MI:SS')", "null");
							
						updateStmt += updateStmtValues;

						updateStmt += " where " + transfer.getUniqueKeyStatement();
						fileWriter.write(updateStmt);
						fileWriter.newLine();
					}

					if (i % 50 == 0) {
						System.out.println("SYNC : " + i + " Records Exported");
					}

				}
				page++;

				transferDao.markStatus(transferIdSubList, exportSqlFile.getName(), "EXPORTED");
				transferIdSubList.clear();
				pendingTransferList = transferDao.getPendingTransfers();
			}
			System.out.println("SYNC : " + i + " Records exported");
			fileWriter.flush();
			fileWriter.close();

			System.out.println("SYNC : Exporting to directory : [ " + dumpDirectoryPathInNew + " ] completed!");

			File dumpDirectoryFileInNew = new File(dumpDirectoryPathInNew);

			String zipFileName = appWorkingDir + File.separator + APP_DIR_NEW + File.separator + fileName + ".zip";
			File targetZipFile = new File(zipFileName);
			ZipUtil.zipFile(dumpDirectoryFileInNew, targetZipFile);
			System.out.println("SYNC : Zip file created : [ " + targetZipFile.getAbsolutePath() + " ] ");

			System.out.println("SYNC : Zip file transfer started: [ " + targetZipFile.getAbsolutePath() + " ] ");
			transferFile(targetZipFile);

			File statusFile = getStatusFile(targetZipFile.getAbsolutePath() + ".status");
			transferFile(statusFile);

			transferDao.markStatus(transferIdList, exportSqlFile.getName(), "SENT");

			FileUtils.forceDelete(statusFile);
			System.out.println("SYNC : Zip file transfer completed: [ " + targetZipFile.getAbsolutePath() + " ] ");
			System.out.println("SYNC : Zip-Status file transfer completed: [ " + statusFile.getAbsolutePath() + " ] ");

			String zipFileRenamedWithSentFolder = appWorkingDir + File.separator + APP_DIR_SENT + File.separator
					+ fileName + ".zip";
			targetZipFile.renameTo(new File(zipFileRenamedWithSentFolder));

			FileUtils.deleteDirectory(dumpDirectoryFileInNew);
			System.out.println("SYNC : Directory : [ " + dumpDirectoryFileInNew.getAbsolutePath() + " ] removed!");

			System.out.println("SYNC : SyncTransfer table records marked as 'SENT' : ");

		} else {
			System.out.println("SYNC : No record found to transfer!");
		}
		System.out.println("SYNC : EXPORT COMPLETED!");
		return pendingTransferList;
	}

	public File getStatusFile(String statusFilePath) throws IOException {
		File f = new File(statusFilePath);
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(f, true));
		fileWriter.write("TransferCompleted");
		fileWriter.flush();
		fileWriter.close();
		return f;

	}

	public void clearFoldersInPreviousExport() throws IOException {
		String destDirPath = appWorkingDir + File.separator + APP_DIR_NEW;
		File destDir = new File(destDirPath);
		File[] destDirFileList = destDir.listFiles();
		if (destDirFileList.length > 0) {
			for (File file : destDirFileList) {
				FileUtils.forceDelete(file);
			}
		}

	}

	public void transferFile(File fileToBeCopied) throws IOException, InterruptedException {

		System.out.println("Copying : " + fileToBeCopied.getAbsolutePath() + " to " + targetSshUser + "@" + targetSshIp
				+ ":" + targetSshFolder + "---- password : " + targetSshPassword);

		ProcessBuilder pbScpCmd = new ProcessBuilder("scp", fileToBeCopied.getAbsolutePath(),
				targetSshUser + "@" + targetSshIp + ":" + targetSshFolder);

		Process processScpCmd = pbScpCmd.start();

		processScpCmd.waitFor();

	}

	public String getProcessOutput(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line + System.getProperty("line.separator"));
		}
		br.close();
		return sb.toString();
	}

}
