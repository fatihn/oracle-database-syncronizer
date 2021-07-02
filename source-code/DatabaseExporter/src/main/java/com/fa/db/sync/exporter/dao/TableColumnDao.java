package com.fa.db.sync.exporter.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.fa.db.sync.exporter.dto.TableColumn;
import com.fa.db.sync.exporter.dto.Transfer;

public interface TableColumnDao {
	public List<TableColumn> getTableColumns(String owner, String tableName);

	public void readLobToFile(Transfer t, TableColumn tc, String dirName)throws SQLException,IOException;
}
