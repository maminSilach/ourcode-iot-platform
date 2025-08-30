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

```bash
make help
```

##  Запуск необходимой инфраструктуры
Откройте .env.example в редакторе, измените пароли/логины по желанию

```bash
 make infra-up
```

#  Проверка работы
```bash
make infra-check
```

# Запуск Spring Boot приложения

Для запуска сервиса при необходимости нужные установите переменные окружения или используйте дефолтные (.properties)

Запустите unit и интеграционные тесты
```bash
make app-test
```

Запустите приложение 
```bash
make app-run
```