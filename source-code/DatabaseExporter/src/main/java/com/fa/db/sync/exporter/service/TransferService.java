package com.fa.db.sync.exporter.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.fa.db.sync.exporter.dto.Transfer;

public interface TransferService {
	List<Transfer> processPendingTransfers() throws SQLException, IOException,InterruptedException;
	
}
