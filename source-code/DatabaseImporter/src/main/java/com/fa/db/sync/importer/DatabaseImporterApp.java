package com.fa.db.sync.importer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatabaseImporterApp {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseImporterApp.class, args);
	}

}

