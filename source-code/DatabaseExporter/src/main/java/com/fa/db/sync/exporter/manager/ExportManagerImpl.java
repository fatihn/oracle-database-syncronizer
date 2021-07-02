package com.fa.db.sync.exporter.manager;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.fa.db.sync.exporter.service.TransferService;

@Component
@Scope("singleton")
public class ExportManagerImpl implements ExportManager, Runnable {

	@Autowired
	ThreadPoolTaskScheduler taskScheduler;
	@Autowired
	private TransferService transferService;
	
	@PostConstruct
	public void initScanner() {
		taskScheduler.schedule(this, new CronTrigger("0 0/1 * * * ?"));

	}

	@Override
	public void run() {
		try {
			transferService.processPendingTransfers();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
