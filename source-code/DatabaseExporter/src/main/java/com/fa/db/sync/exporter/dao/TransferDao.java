package com.fa.db.sync.exporter.dao;

import java.util.List;

import com.fa.db.sync.exporter.dto.Transfer;

public interface TransferDao {
	public List<Transfer> getPendingTransfers();
	public List<String> getPendingTransferTableNames();
	public String getInsertStmtValues(String sql);
	public String getUpdateStmtValues(String sql);
	public void markStatus(List<Long> idList, String fileName, String status);
	public void markExportedStatusAsPending() ;
}
