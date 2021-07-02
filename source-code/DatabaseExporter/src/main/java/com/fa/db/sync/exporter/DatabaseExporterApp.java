package com.fa.db.sync.exporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatabaseExporterApp {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseExporterApp.class, args);
	}
}
