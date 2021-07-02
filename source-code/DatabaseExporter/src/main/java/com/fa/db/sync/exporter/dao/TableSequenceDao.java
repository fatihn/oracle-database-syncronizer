package com.fa.db.sync.exporter.dao;

import java.util.List;
import java.util.Map;

import com.fa.db.sync.exporter.dto.TableSequence;

public interface TableSequenceDao {

	public Map<String, String> getTableSequences();
}
