package com.fa.db.sync.exporter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.fa.db.sync.exporter.dto.Transfer;

public class TransferRowMapper implements RowMapper<Transfer> {
	@Override
	public Transfer mapRow(ResultSet row, int rowNum) throws SQLException {
		Transfer transfer = new Transfer();
		transfer.setId(row.getLong("id"));
		transfer.setTableOwner(row.getString("table_owner"));
		transfer.setTableName(row.getString("table_name"));
		transfer.setUniqueKeyStatement(row.getString("unique_key_statement"));
		transfer.setOpType(row.getString("op_type"));
		transfer.setCreationDate(row.getDate("creation_date"));
		transfer.setTransferDate(row.getDate("transfer_date"));
		transfer.setTransferStatusId(row.getString("transfer_status_id"));
		transfer.setTransferStatusDesc(row.getString("transfer_status_desc"));
		transfer.setTransferFileName(row.getString("transfer_file_name"));
		return transfer;
	}
}