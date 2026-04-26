package com.vinayak.project.dagOrchestartor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DagOrchestartorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DagOrchestartorApplication.class, args);
	}

}
