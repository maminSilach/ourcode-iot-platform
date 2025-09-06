.PHONY: infra-up infra-check app-test app-run help

INFRA_DIR = ../infrastructure
APP_DIR = ../device-collector
DOCKER_COMPOSE = docker-compose

infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up zookeeper kafka schema-registry kafka-ui postgres postgres2 postgres-replica postgres2-replica prometheus grafana -d
	@echo "Infrastructure is starting..."

infra-check:
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
	cd $(APP_DIR)

app-test:
	@echo "Running unit and integration tests..."
	.\gradlew test

app-run:
	@echo "Starting Spring Boot application..."
	.\gradlew bootRun

help:
	@echo "Available commands:"
	@echo "  make infra-up     - Start infrastructure"
	@echo "  make infra-check  - Check infrastructure status"
	@echo "  make app-test     - Run application tests"
	@echo "  make app-run      - Run Spring Boot application"
	@echo "  make help         - Show this help"