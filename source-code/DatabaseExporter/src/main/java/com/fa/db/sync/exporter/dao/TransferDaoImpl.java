package com.fa.db.sync.exporter.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import com.fa.db.sync.exporter.dto.Transfer;

@Transactional
@Repository
public class TransferDaoImpl implements TransferDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Transfer> getPendingTransfers() {
		String sql = "SELECT * FROM ecsms.sync_transfer where transfer_status_id='PENDING' order by id asc";
		RowMapper<Transfer> rowMapper = new TransferRowMapper();
		jdbcTemplate.setMaxRows(50);
		return this.jdbcTemplate.query(sql, rowMapper);
	}

	@Override
	public List<String> getPendingTransferTableNames() {
		String sql = "SELECT table_name FROM ecsms.sync_transfer where transfer_status_id='PENDING' group by table_name";
		List<String> tableNames = jdbcTemplate.query(sql, new BeanPropertyRowMapper(String.class));
		return tableNames;

	}

	@Override
	public String getUpdateStmtValues(String sql) {
		// System.out.println("### : " + sql);
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
		String val = "";
		int i = 1;
		SqlRowSetMetaData rsmd = rowSet.getMetaData();
		while (rowSet.next()) {
			while (i <= rsmd.getColumnCount()) {
				String sqlColumnType = rsmd.getColumnTypeName(i);
				val += rsmd.getColumnName(i) + "=";
				
				if ((sqlColumnType.contains("CHAR") || sqlColumnType.contains("DATE")) && rowSet.getString(i) != null)
					val += "\'";
				val += escapeStr(rowSet.getString(i));
				if ((sqlColumnType.contains("CHAR") || sqlColumnType.contains("DATE")) && rowSet.getString(i) != null)
					val += "\'";

				val += " ,";
				i++;

			}

		}
		if (val.length() > 0)
			val = val.substring(0, val.length() - 1);

		return val;

	}
	@Override
	public String getInsertStmtValues(String sql) {
		// System.out.println("### : " + sql);
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
		String val = "";
		int i = 1;
		SqlRowSetMetaData rsmd = rowSet.getMetaData();
		while (rowSet.next()) {
			while (i <= rsmd.getColumnCount()) {
				String sqlColumnType = rsmd.getColumnTypeName(i);

				if ((sqlColumnType.contains("CHAR") || sqlColumnType.contains("DATE")) && rowSet.getString(i) != null)
					val += "\'";
				val += escapeStr(rowSet.getString(i));
				if ((sqlColumnType.contains("CHAR") || sqlColumnType.contains("DATE")) && rowSet.getString(i) != null)
					val += "\'";

				val += ",";
				i++;

			}

		}
		if (val.length() > 0)
			val = val.substring(0, val.length() - 1);

		return val;

	}

	public String escapeStr(String str) {
		if(str != null)
			str = str.replace("'", "''");
	
		return str;
	}

	@Override
	public void markExportedStatusAsPending() {
		String sql = "update ecsms.sync_transfer " + "	     set transfer_date = null, "
				+ " 		 transfer_status_id='PENDING', " + "			 transfer_status_desc='PENDING', "
				+ "          transfer_file_name='null'" + "    where transfer_status_id='EXPORTED'";

		jdbcTemplate.execute(sql);

	}

	@Override
	public void markStatus(List<Long> idList, String fileName, String status) {
		List<Object[]> batch = new ArrayList<Object[]>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		String nowDateStr = sdf.format(new Date());

		for (Long id : idList) {
			Object[] values = new Object[] { id };
			batch.add(values);
		}
		String sql = "update ecsms.sync_transfer " + "	     set transfer_date =  '" + nowDateStr + "', "
				+ " 		 transfer_status_id='" + status + "', " + "			 transfer_status_desc='" + status
				+ "', " + "          transfer_file_name='" + fileName + "'" + "    where id = ?";

		jdbcTemplate.batchUpdate(sql, batch);

	}

}
