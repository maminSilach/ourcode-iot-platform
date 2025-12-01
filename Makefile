.PHONY: help
.PHONY: device-collector-infra-up device-collector-infra-check device-collector-app-test device-collector-app-run
.PHONY: events-collector-infra-up events-collector-infra-check events-collector-app-test events-collector-app-run
.PHONY: device-service-infra-up device-service-infra-check device-service-app-test device-service-app-run
.PHONY: router-manager-service-infra-up router-manager-service-infra-check router-manager-service-app-test device-service-app-run

INFRA_DIR = ./infrastructure
DEVICE_COLLECTOR_APP_DIR = ./device-collector
EVENTS_COLLECTOR_APP_DIR = ./events-collector-service
DEVICE_SERVICE_APP_DIR = ./device-service
ROUTER_MANAGER_APP_DIR = ./router-manager-service
DOCKER_COMPOSE = docker-compose

device-service-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up keycloak nexus nexus-change-password postgres postgres2 postgres-replica postgres2-replica prometheus grafana -d
	@echo "Infrastructure is starting..."

device-service-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Testing Postgres Shard1..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Replica Shard1..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres-replica psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Shar2..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres2 psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Replica Shard2..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres2-replica psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Checking Prometheus targets..."
	curl http://localhost:9090/api/v1/targets

	@echo "Checking Grafana health..."
	curl http://localhost:3000/api/health

	@echo "Checking Nexus health..."
	curl http://localhost:8081

	@echo "Back to Service Directory"
	cd $(./)

device-service-app-test:
	@echo "Running unit and integration tests..."
	cd $(DEVICE_SERVICE_APP_DIR) && .\gradlew test

device-service-app-run:
	@echo "Starting Spring Boot application..."
	cd $(DEVICE_SERVICE_APP_DIR) && gradlew bootRun

device-collector-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up zookeeper kafka schema-registry kafka-ui postgres postgres2 postgres-replica postgres2-replica prometheus grafana -d
	@echo "Infrastructure is starting..."

device-collector-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Checking Kafka topics..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec kafka kafka-topics --list --bootstrap-server localhost:9092

	@echo "Checking Schema Registry..."
	curl -X GET http://localhost:8081/subjects

	@echo "Testing Postgres Shard1..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Replica Shard1..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres-replica psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Shar2..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres2 psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Testing Postgres Replica Shard2..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres2-replica psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Checking Prometheus targets..."
	curl http://localhost:9090/api/v1/targets

	@echo "Checking Grafana health..."
	curl http://localhost:3000/api/health

	@echo "Back to Service Directory"
	cd $(./)

device-collector-app-test:
	@echo "Running unit and integration tests..."
	cd $(DEVICE_COLLECTOR_APP_DIR) && .\gradlew test

device-collector-app-run:
	@echo "Starting Spring Boot application..."
	cd $(DEVICE_COLLECTOR_APP_DIR) && gradlew bootRun

events-collector-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up zookeeper kafka schema-registry kafka-ui cassandra -d
	@echo "Infrastructure is starting..."

events-collector-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Checking Kafka topics..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec kafka kafka-topics --list --bootstrap-server localhost:9092

	@echo "Checking Schema Registry..."
	curl -X GET http://localhost:8081/subjects

	@echo "Checking Cassandra..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

	@echo "Back to Service Directory"
	cd $(./)

events-collector-app-test:
	@echo "Running unit and integration tests..."
	cd $(EVENTS_COLLECTOR_APP_DIR) && .\gradlew test

events-collector-app-run:
	@echo "Starting Spring Boot application..."
	cd $(EVENTS_COLLECTOR_APP_DIR) && gradlew bootRun

router-manager-service-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	cp .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up postgres -d
	@echo "Infrastructure is starting..."

router-manager-service-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Testing Postgres"
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec postgres psql -U postgres -c "SELECT pg_is_in_recovery();"

	@echo "Back to Service Directory"
	cd $(./)

router-manager-service-app-test:
	@echo "Running unit and integration tests..."
	cd $(ROUTER_MANAGER_APP_DIR) && go test ./... -v

router-manager-service-app-run:
	@echo "Starting Spring Boot application..."
	cd $(ROUTER_MANAGER_APP_DIR) && go run ./cmd/router-manager-service

help:
	@echo "Available commands:"
	@echo "  make device-collector-infra-up     - Start infrastructure Device Collector"
	@echo "  make device-collector-infra-check  - Check infrastructure status of Device Collector"
	@echo "  make device-collector-app-test     - Run application tests of Device Collector"
	@echo "  make device-collector-app-run     - Run Spring Boot Device Collector Application"

	@echo "  make events-collector-infra-up     - Start infrastructure Events Collector"
	@echo "  make events-collector-infra-check  - Check infrastructure status of Events Collector"
	@echo "  make events-collector-app-test     - Run application tests of Events Collector"
	@echo "  make events-collector-app-run     - Run Spring Boot Events Collector Application"

	@echo "  make device-service-infra-up     - Start infrastructure Device Service"
	@echo "  make device-service-infra-check  - Check infrastructure status of Device Service"
	@echo "  make device-service-app-test    - Run application tests of Device Service"
	@echo "  make device-service-app-run     - Run Spring Boot Device Service Application"

	@echo "  make router-manager-service-infra-up     - Start infrastructure Router Manager Service"
	@echo "  make router-manager-service-infra-check  - Check infrastructure status of Router Manager Service"
	@echo "  make router-manager-service-app-test    - Run application tests of Router Manager Service"
	@echo "  make router-manager-service-app-run    - Run Spring Boot Router Manager Application"

	@echo "  make help         - Show this help"