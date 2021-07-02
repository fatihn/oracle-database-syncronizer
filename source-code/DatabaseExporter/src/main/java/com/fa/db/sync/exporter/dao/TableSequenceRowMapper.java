package com.fa.db.sync.exporter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.fa.db.sync.exporter.dto.TableSequence;

public class TableSequenceRowMapper implements RowMapper<TableSequence> {
	@Override
	public TableSequence mapRow(ResultSet row, int rowNum) throws SQLException {
		TableSequence column = new TableSequence();
		column.setTableName(row.getString("table_name"));
		column.setSequenceName(row.getString("sequence_name"));

		return column;
	}
}