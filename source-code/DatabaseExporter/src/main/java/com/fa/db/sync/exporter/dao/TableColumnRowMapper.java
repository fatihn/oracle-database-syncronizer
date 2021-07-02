package com.fa.db.sync.exporter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.fa.db.sync.exporter.dto.TableColumn;

public class TableColumnRowMapper implements RowMapper<TableColumn> {
	@Override
	public TableColumn mapRow(ResultSet row, int rowNum) throws SQLException {
		TableColumn column = new TableColumn();
		column.setColumnId(row.getInt("column_id"));
		column.setTableName(row.getString("table_name"));
		column.setColumnName(row.getString("column_name"));
		column.setDataType(row.getString("data_type"));
		column.setDataPrecision(row.getInt("data_precision"));
		column.setDataScale(row.getInt("data_scale"));
		return column;
	}
}