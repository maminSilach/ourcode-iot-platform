package com.example.apiorchestrator;

import com.example.deviceservice.api.DeviceApi;
import com.example.eventservice.api.EventApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = {EventApi.class, DeviceApi.class})
@EnableWebFluxSecurity
public class ApiOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiOrchestratorApplication.class, args);
	}

}
