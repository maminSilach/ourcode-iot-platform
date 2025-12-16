.PHONY: help
.PHONY: device-collector-infra-up device-collector-infra-check device-collector-app-test device-collector-app-run
.PHONY: events-collector-infra-up events-collector-infra-check events-collector-app-test events-collector-app-run
.PHONY: device-service-infra-up device-service-infra-check device-service-app-test device-service-app-run
.PHONY: failed-event-processor-infra-up failed-event-processor-infra-check failed-event-processor-app-test failed-event-processor-app-run
.PHONY: event-service-infra-up events-service-infra-check event-service-app-test event-service-app-run
.PHONY: api-orchestrator-infra-up api-orchestrator-infra-check api-orchestrator-app-test api-orchestrator-app-run

INFRA_DIR = ./infrastructure
DEVICE_COLLECTOR_APP_DIR = ./device-collector
EVENTS_COLLECTOR_APP_DIR = ./events-collector-service
DEVICE_SERVICE_APP_DIR = ./device-service
FAILED_EVENTS_PROCESSOR_APP_DIR = ./failed-events-processor
EVENT_SERVICE_APP_DIR = ./event-service
API_ORCHESTRATOR_APP_DIR = ./api-orchestrator
DOCKER_COMPOSE = docker-compose

device-service-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	cp .env.example .env && \
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

failed-event-processor-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up zookeeper kafka schema-registry kafka-ui minio createbuckets prometheus grafana -d

	@echo "Infrastructure is starting..."

failed-event-processor-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Checking Kafka topics..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec kafka kafka-topics --list --bootstrap-server localhost:9092

	@echo "Checking Schema Registry..."
	curl -X GET http://localhost:8081/subjects

	@echo "Checking Prometheus targets..."
	curl http://localhost:9090/api/v1/targets

	@echo "Checking Grafana health..."
	curl http://localhost:3000/api/health

	@echo "Back to Service Directory"
	cd $(./)

failed-event-processor-app-test:
	@echo "Running unit and integration tests..."
	cd $(FAILED_EVENTS_PROCESSOR_APP_DIR) && .\gradlew test

failed-event-processor-app-run:
	@echo "Starting Spring Boot application..."
	cd $(FAILED_EVENTS_PROCESSOR_APP_DIR) && gradlew bootRun

event-service-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	cp .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up cassandra nexus nexus-change-password prometheus grafana -d
	@echo "Infrastructure is starting..."

events-service-infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Checking Cassandra..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

	@echo "Checking Prometheus targets..."
	curl http://localhost:9090/api/v1/targets

	@echo "Checking Grafana health..."
	curl http://localhost:3000/api/health

	@echo "Checking Nexus health..."
	curl http://localhost:8081

	@echo "Back to Service Directory"
	cd $(./)

event-service-app-test:
	@echo "Running unit and integration tests..."
	cd $(EVENT_SERVICE_APP_DIR) && ./gradlew test

event-service-app-run:
	@echo "Starting Spring Boot application..."
	cd $(EVENT_SERVICE_APP_DIR) && ./gradlew bootRun

api-orchestrator-infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	cp .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up cassandra nexus nexus-change-password prometheus grafana keycloak postgres postgres2 postgres-replica postgres2-replica device-service event-service -d

api-orchestrator-infra-check:
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

	@echo "Checking Cassandra..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

	@echo "Back to Service Directory"
	cd $(./)

api-orchestrator-app-test:
	@echo "Running unit and integration tests..."
	cd $(API_ORCHESTRATOR_APP_DIR) && ./gradlew test

api-orchestrator-app-run:
	@echo "Starting Spring Boot application..."
	cd $(API_ORCHESTRATOR_APP_DIR) && ./gradlew bootRun

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

	@echo "  make failed-event-processor-infra-up     - Start infrastructure Failed Event Processor"
	@echo "  make failed-event-processor-infra-check  - Check infrastructure status of Failed Event Processor"
	@echo "  make failed-event-processor-app-test    - Run application tests of Failed Event Processor"
	@echo "  make failed-event-processor-app-run    - Run Spring Boot Failed Event Processor Application"

	@echo "  make event-service-infra-up    - Start infrastructure Event Service"
	@echo "  make events-service-infra-check  - Check infrastructure status of Event Service"
	@echo "  make event-service-app-test    - Run application tests of Event Service"
	@echo "  make event-service-app-run   - Run Spring Boot Event Service Application"

	@echo "  make api-orchestrator-infra-up   - Start infrastructure Api Orchestrator Service"
	@echo "  make api-orchestrator-infra-check - Check infrastructure status of Api Orchestrator Service"
	@echo "  make api-orchestrator-app-test   - Run application tests of Api Orchestrator Service"
	@echo "  make api-orchestrator-app-run - Run Spring Boot Api Orchestrator Service"

	@echo "  make help         - Show this help"