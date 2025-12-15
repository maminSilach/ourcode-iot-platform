package com.example.apiorchestrator;

import org.springframework.boot.SpringApplication;

public class TestApiOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApiOrchestratorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
