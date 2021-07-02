package com.fa.db.sync.exporter.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fa.db.sync.exporter.dto.TableSequence;

@Transactional
@Repository
public class TableSequenceDaoImpl implements TableSequenceDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Map<String, String> getTableSequences() {
		String sql = "select table_name, sequence_name from ecsms.sync_table_sequence where sequence_name is not null";
		RowMapper<TableSequence> rowMapper = new TableSequenceRowMapper();
		jdbcTemplate.setMaxRows(1000);
		List<TableSequence> seqList = this.jdbcTemplate.query(sql, rowMapper);
		
		Map<String, String> sequenceMap = new HashMap<String,String>();
		
		for(TableSequence ts : seqList) {
			sequenceMap.put(ts.getTableName(), ts.getSequenceName());
		}
		
		return sequenceMap;
	}

}
