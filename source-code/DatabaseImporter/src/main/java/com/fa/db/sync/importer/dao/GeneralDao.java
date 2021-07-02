package com.fa.db.sync.importer.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public class GeneralDao {
	public void processLobFile(Connection conn, String lobFileName, String lobFilePath)
			throws IOException, SQLException {
		String[] fileTokens = lobFileName.split("#");

		String schema = fileTokens[0];
		String table = fileTokens[1];
		String columnName = fileTokens[2];
		String uniqueKeyStatement = fileTokens[3].substring(0, fileTokens[3].length() - 4);

		String sqlStr = "update " + schema + "." + table + " set " + columnName + "=? where " + uniqueKeyStatement;

		PreparedStatement pstmt = conn.prepareStatement(sqlStr);
		File blob = new File(lobFilePath);
		FileInputStream in = new FileInputStream(blob);

		pstmt.setBinaryStream(1, in, (int) blob.length());
		pstmt.executeUpdate();

		pstmt.close();
		in.close();

	}
}
