# Failed Events Processor

failed-events-processor это микросервис, который:

- **Подписывается** на Kafka Dead Letter Topic (DLT) и получает сообщения о событиях, которые не удалось обработать другими сервисами.
- **Формирует** на основе этих сообщений структурированные JSON-файлы.
- **Сохраняет** полученные JSON-файлы в объектное хранилище MinIO.
- **Экспортирует** метрики и healthcheck для мониторинга.


## Технологии

- **Java 24** - Язык программирования
- **Spring Boot 3.5** - Фреймворк приложения
- **Apache Kafka** - Система обмена сообщениями
- **Apache Avro** - Формат сериализации данных
- **Confluent Schema Registry** - Валидация и эволюция схем
- **MiniO** - Объектное S3 хранилище
- **Testcontainers** - Фреймворк для интеграционного тестирования
- **Gradle** - Система сборки

## Архитектура

    A[Kafka топик events] --> B[Failed Events Processor]
    B --> C[Валидация через Schema Registry]
    C --> D[Сохранение в MiniO]
    D --> F[Поворные попытки в случае неуспеха]

# Инструкция по локальному запуску

```bash
make help
```

##  Запуск необходимой инфраструктуры
Откройте .env.example в редакторе, измените пароли/логины по желанию

```bash
make failed-event-processor-infra-up
```

#  Проверка работы
```bash
make failed-event-processor-infra-check
```

# Запуск Spring Boot приложения

Для запуска сервиса при необходимости нужные установите переменные окружения или используйте дефолтные (.properties)

Запустите unit и интеграционные тесты
```bash
make failed-event-processor-app-test
```

Запустите приложение
```bash
make failed-event-processor-app-run
```