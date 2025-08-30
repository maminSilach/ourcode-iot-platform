.PHONY: infra-up infra-check app-test app-run help

INFRA_DIR = ../infrastructure
APP_DIR = ../events-collector-service
DOCKER_COMPOSE = docker-compose

infra-up:
	@echo "Moving to infrastructure directory..."
	cd $(INFRA_DIR) && \
	copy .env.example .env && \
	echo "Open .env in editor and change passwords/logins if needed" && \
	$(DOCKER_COMPOSE) up zookeeper kafka schema-registry kafka-ui cassandra -d
	@echo "Infrastructure is starting..."

infra-check:
	@echo "Checking container status..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) ps

	@echo "Checking Kafka topics..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec kafka kafka-topics --list --bootstrap-server localhost:9092

	@echo "Checking Schema Registry..."
	curl -X GET http://localhost:8081/subjects

	@echo "Checking Cassandra..."
	cd $(INFRA_DIR) && $(DOCKER_COMPOSE) exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

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