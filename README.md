# IoT Microservice Platform
IoT Microservice Platform – учебный проект, демонстрирующий архитектуру микросервисов для IoT. Содержит инфраструктуру (Kafka, PostgreSQL, и др.) и пример реализации процессов IoT

## Локальный запуск

Клонирование репозитория
```bash
  git clone https://link-to-project
```

Переход в корневую директорию
```bash
  cd ourcode-iot-platform
```

Откройте .env в редакторе, измените пароли/логины по желанию
```bash
  cp infrastructure/.env.example infrastructure/.env
```

Запуск сервисов через Docker Compose:

```bash
  docker-compose up -d
```

Проверка работы: убедитесь, что все контейнеры запущены:
```bash
  docker-compose ps
```


– Откройте браузер: Grafana доступна по ссылке [http://localhost:3000](http://localhost:3000) (логин/пароль по умолчанию: `admin` / `admin` если не меняли).

– Keycloak: [http://localhost:8080](http://localhost:8080) – админ-консоль Keycloak. Войдите с креденшелами `admin` / `admin` (как задано в .env).

Завершение работы: чтобы остановить и удалить контейнеры, выполните:    
```bash
  docker-compose down
```

## Ссылки на localhost-порты для каждого сервиса

- PostgreSQL (localhost:5432)
- Zookeeper (localhost:2181)
- Kafka (localhost:9092)
- Redis (localhost:6379)
- Kafka UI (localhost:8070)
- Grafana (localhost:3000)
- Prometheus (localhost:9090)
- MiniO (localhost:9090 API, localhost:9091 UI)
- Cassandra (localhost:9042)
- Schema Registry(Avro) (localhost:8081)
- Keycloak (localhost:8080)
- Camunda (localhost:8088)
- Kafka Exporter (localhost:9308)
- Postgres Exporter (localhost:9187)

**Замечание**: Порты взяты из наших примеров. В реальном docker-compose могут использоваться другие маппинги. Всегда можно открыть сам `docker-compose.yaml` и посмотреть секцию `ports` у интересующего сервиса.

