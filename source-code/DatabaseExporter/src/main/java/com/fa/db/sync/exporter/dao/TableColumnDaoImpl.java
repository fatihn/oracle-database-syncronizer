package com.fa.db.sync.exporter.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fa.db.sync.exporter.dto.TableColumn;
import com.fa.db.sync.exporter.dto.Transfer;

@Transactional
@Repository
public class TableColumnDaoImpl implements TableColumnDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<TableColumn> getTableColumns(String owner, String tableName) {
		String sql = "SELECT column_id, table_name, column_name, data_type, data_precision, data_scale "
				+ "FROM DBA_TAB_COLUMNS " + "WHERE table_name='" + tableName + "'  and owner = '" + owner + "' "
				+ "order by column_id";
		RowMapper<TableColumn> rowMapper = new TableColumnRowMapper();
		jdbcTemplate.setMaxRows(1000);
		return this.jdbcTemplate.query(sql, rowMapper);
	}
	public void readLobToFile(Transfer t, TableColumn tc, String dirName) throws SQLException, IOException {
		String sql = "SELECT " + tc.getColumnName() + " FROM " + t.getTableOwner() + "." + t.getTableName() + " where "
				+ t.getUniqueKeyStatement();

		PreparedStatement stmt = this.jdbcTemplate.getDataSource().getConnection().prepareStatement(sql);
		ResultSet resultSet = stmt.executeQuery();
		while (resultSet.next()) {

			byte[] buffer = new byte[1];
			InputStream is = resultSet.getBinaryStream(1);
			if (is != null) {
				File file = new File(dirName + "/" + t.getTableOwner() + "#" + t.getTableName() +"#"+tc.getColumnName()+ "#"
						+ t.getUniqueKeyStatement()+".lob");
				FileOutputStream fos = new FileOutputStream(file);

				while (is.read(buffer) > 0) {
					fos.write(buffer);
				}
				fos.close();
				is.close();
			}
			

		}
		resultSet.close();
		stmt.close();
	}
}
