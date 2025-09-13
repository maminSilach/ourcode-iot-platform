# Device Collector Service

Device Collector Service – сервис, который получает события о новых/изменённых устройствах из Apache Kafka, десериализует их с помощью Avro (с контролем схем через Schema Registry), сохраняет информацию об устройствах в шардированную базу данных PostgreSQL (с управлением шардами через Apache ShardingSphere), обеспечивая при этом идемпотентность операций, обработку ошибок (включая механизмы повторных попыток и Dead Letter Topic), экспорт метрик и поддержание показателей health-check.
## Обзор

Device Collector Service — это отказоустойчивый, высокомасштабируемый микросервис, предназначенный для обработки потоков событий от устройств (IoT, мобильные устройства и т.д.). Сервис потребляет события из Apache Kafka, обогащает их и сохраняет в горизонтально масштабируемое хранилище на базе PostgreSQL с использованием Apache ShardingSphere для автоматического шардинга данных.

Ключевые характеристики:

Горизонтальная масштабируемость: Обработка событий за счет масштабирования потребителей Kafka и использования шардированной PostgreSQL.

Отказоустойчивость: Повторные попытки обработки, Dead Letter Topic (DLT) для проблемных сообщений.

Production-Grade: Полное покрытие тестами (unit, integration), мониторинг, подробная документация.

## Технологии

- **Java 24** - Язык программирования
- **Spring Boot 3.5** - Фреймворк приложения
- **Apache Kafka** - Система обмена сообщениями
- **Apache Avro** - Формат сериализации данных
- **Confluent Schema Registry** - Валидация и эволюция схем
- **Postgres** - Распределенная база данных
- **Apache Shardng Sphere** - Решение для промежуточного взаимодействия с распределенной базой данных
- **Testcontainers** - Фреймворк для интеграционного тестирования
- **Gradle** - Система сборки

## Архитектура

    A[Kafka топик device-id] --> B[Device Collector Service]
    B --> C[Валидация через Schema Registry]
    C --> D[Идемпотентное сохранение в Postgres]
    B --> E[Обработка ошибок с Retry with backoff в случае ошибки]
    E --> F[Kafka DLQ топик device]

# Данные
## Входное событие

```json
{
    "type": "record",
    "name": "DeviceEvent",
    "namespace": "com.example.avro",
    "fields": [
        {"name": "deviceId", "type": "string"},
        {"name": "deviceType", "type": "string"},
        {"name": "createdAt", "type": "long"},
        {"name": "meta", "type": "string"}
    ]
}
```

# Развертывание и эксплуатация
## Мониторинг

Необходимо в Grafana добавить источник данных в виде Prometheus

Сервис предоставляет метрики через эндпоинт Spring Boot Actuator /actuator/prometheus:

kafka.listener.seconds: Тайминг обработки сообщений

kafka.consumer.records.consumed: Количество consumed сообщений

jvm.memory.used: Использование памяти JVM

messages.failed.total: Количество ошибочных сообщений, попавших в DLQ

liveness/readiness пробы

# Инструкция по локальному запуску

```bash
make help
```

##  Запуск необходимой инфраструктуры
Откройте .env.example в редакторе, измените пароли/логины по желанию

```bash
make device-collector-infra-up
```

#  Проверка работы
```bash
make device-collector-infra-check
```

# Запуск Spring Boot приложения

Для запуска сервиса при необходимости нужные установите переменные окружения или используйте дефолтные (.properties)

Запустите unit и интеграционные тесты
```bash
make device-collector-app-test
```

Запустите приложение
```bash
make device-collector-app-run
```