package com.fa.db.sync.exporter.dto;

import java.util.Date;

public class Transfer {
	Long id;
	String tableOwner;
	String tableName;
	String uniqueKeyStatement;
	String opType;
	Date creationDate;
	Date transferDate;
	String transferStatusId;
	String transferStatusDesc;
	String transferFileName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTableOwner() {
		return tableOwner;
	}
	public void setTableOwner(String tableOwner) {
		this.tableOwner = tableOwner;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getUniqueKeyStatement() {
		return uniqueKeyStatement;
	}
	public void setUniqueKeyStatement(String uniqueKeyStatement) {
		this.uniqueKeyStatement = uniqueKeyStatement;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(Date transferDate) {
		this.transferDate = transferDate;
	}
	public String getTransferStatusId() {
		return transferStatusId;
	}
	public void setTransferStatusId(String transferStatusId) {
		this.transferStatusId = transferStatusId;
	}
	public String getTransferStatusDesc() {
		return transferStatusDesc;
	}
	public void setTransferStatusDesc(String transferStatusDesc) {
		this.transferStatusDesc = transferStatusDesc;
	}
	public String getTransferFileName() {
		return transferFileName;
	}
	public void setTransferFileName(String transferFileName) {
		this.transferFileName = transferFileName;
	}	
	
	
	
	
}
