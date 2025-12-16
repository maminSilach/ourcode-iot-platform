package com.example.apiorchestrator.integration;

import com.example.apiorchestrator.ApiOrchestratorApplication;
import com.example.apiorchestrator.adapter.out.RouterManagerClientImpl;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@ActiveProfiles("test")
@SpringBootTest(classes = ApiOrchestratorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiOrchestratorApplicationTests extends TestcontainersConfiguration {

	@MockitoBean
	public RouterManagerClientImpl routerManagerClient;

	@MockitoBean
	public ManagedChannel managedChannel;

	@Autowired
	protected WebTestClient webTestClient;
}
