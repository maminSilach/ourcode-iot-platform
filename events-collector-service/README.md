# Event Collector Service

Микросервис для сбора и обработки событий устройств с использованием Apache Kafka, Apache Avro и Apache Cassandra.

## Обзор

Event Collector Service — это Spring Boot приложение, которое:

- **Подписывается** на Kafka-топик в формате Avro
- **Валидирует** события через Confluent Schema Registry
- **Сохраняет** события в Apache Cassandra для аналитических целей
- **Публикует** уникальные device_id в отдельный топик

## Технологии

- **Java 24** - Язык программирования
- **Spring Boot 3.5** - Фреймворк приложения
- **Apache Kafka** - Система обмена сообщениями
- **Apache Avro** - Формат сериализации данных
- **Confluent Schema Registry** - Валидация и эволюция схем
- **Apache Cassandra** - Распределенная база данных
- **Testcontainers** - Фреймворк для интеграционного тестирования
- **Gradle** - Система сборки

## Архитектура

    A[Kafka топик events] --> B[Event Collector Service]
    B --> C[Валидация через Schema Registry]
    C --> D[Сохранение в Cassandra]
    B --> E[Дедупликация Device ID]
    E --> F[Kafka топик deviceId]
    
# Инструкция по локальному запуску

##  Запуск необходимой инфраструктуры

Переход в директорию с инфраструктурой
```bash
 cd ../infrastructure
```

Откройте .env в редакторе, измените пароли/логины по желанию
```bash
cp .env.example .env
```

Запуск сервисов через Docker Compose:

```bash
 docker-compose up zookeeper kafka schema-registry kafka-ui cassandra -d
```

#  Проверка работы

Убедитесь, что все контейнеры запущены

```bash
docker-compose ps
```

Проверить Kafka

```bash
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

Проверить Schema Registry

```bash
curl -X GET http://localhost:8081/subjects
```

Альтернатива для PowerShell
```bash
iwr -Uri "http://localhost:8081/subjects" -Method Get
```

Проверить Cassandra

```bash
docker-compose exec cassandra cqlsh -e "DESCRIBE KEYSPACES"
```

– Откройте браузер: Kafka UI доступна по ссылке [http://localhost:8070](http://localhost:8070)

# Запуск Spring Boot приложения

Переход в корневую директорию сервиса

```bash
cd ../events-collector-service
```

Для запуска сервиса при необходимости нужные установите переменные окружения или используйте дефолтные (.properties)

Запустите unit и интеграционные тесты
```bash
./gradlew test 
```

Запустите приложение 
```bash
./gradlew bootRun
```