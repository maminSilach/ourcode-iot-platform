# Device Service

Device Service – сервис, который получает события о новых/изменённых устройствах по REST, сохраняет информацию об устройствах в шардированную базу данных PostgreSQL (с управлением шардами через Apache ShardingSphere), обеспечивая при этом идемпотентность операций, обработку ошибок (включая механизмы повторных попыток и Dead Letter Topic), экспорт метрик, поддержание показателей health-check и использует протокол OAuth 2.0 для аутентификации клиента.
## Обзор

Device Service — это отказоустойчивый, высокомасштабируемый микросервис, предназначенный для обработки входящих запросов (IoT, мобильные устройства и т.д.). Сервис обрабатывает HTTP запросы, и сохраняет в горизонтально масштабируемое хранилище на базе PostgreSQL с использованием Apache ShardingSphere для автоматического шардинга данных.
При запуске собирается и экспортируется библиотека device-client в Nexus репозиторий. 

## Технологии

- **Java 24** - Язык программирования
- **Spring Boot 3.5** - Фреймворк приложения
- **Postgres** - Распределенная база данных
- **KeyCloak** - Единая точка входа для аутентификации/авторизации
- **Nexus** - Централизованное хранилище библиотек
- **Apache Shardng Sphere** - Решение для промежуточного взаимодействия с распределенной базой данных
- **Testcontainers** - Фреймворк для интеграционного тестирования
- **Gradle** - Система сборки

## Архитектура

    A[Клиент] --> B[Device Service]
    B --> C[Проверка подписи токена]
    C --> D[Идемпотентное сохранение в Postgres]
    B --> E[Обработка ошибок в случае ошибки]
    E --> A[Выдача ответа]

# Данные
## Входное событие

```json
{
  "id": "id",
  "deviceType": "type",
  "createdAt": 1000,
  "meta": "meta"
}
```

Перед этим необходимо получить токен. Дефолтный адрес: (http://localhost:8080/realms/out-platform/protocol/openid-connect/token)

Тело: x-www-form-urlencoded
```json
{
  "grant_type": "password",
  "client_id": "device-client",
  "username": "test",
  "password": "test"
}
```
Либо по grant_type: client_credentials, перед этим узнав в UI KeyCloak (http://localhost:8080) значение client_secret
```json
{
  "grant_type": "client_credentials",
  "client_id": "device-client",
  "client_secret": "ваш client_secret"
}
```
# Развертывание и эксплуатация

## Публикация библиотеки
При запуске build в поднятый центральный репозиторий кладется библиотека device-client (http://localhost:8081).
(логин/пароль по умолчанию: `admin` / `admin`).

## Мониторинг

Необходимо в Grafana добавить источник данных в виде Prometheus

Сервис предоставляет метрики через эндпоинт Spring Boot Actuator /actuator/prometheus

messages.failed.total.*: Количество ошибочных сообщений

liveness/readiness пробы

# Инструкция по локальному запуску

```bash
make help
```

##  Запуск необходимой инфраструктуры
Откройте .env.example в редакторе, измените пароли/логины по желанию

```bash
make device-service-infra-up
```

#  Проверка работы
```bash
make device-service-infra-check
```

# Запуск Spring Boot приложения

Для запуска сервиса при необходимости нужные установите переменные окружения или используйте дефолтные (.properties)

Запустите unit и интеграционные тесты
```bash
make device-service-app-test
```

Запустите приложение
```bash
make device-service-app-run
```