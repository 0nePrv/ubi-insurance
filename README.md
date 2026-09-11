# UBI Insurance

Страхование по стилю вождения (usage-based insurance): телематика → скоринг водителя → динамическая премия →
урегулирование убытков. Учебно-демонстрационный проект: микросервисы, event sourcing, саги, CQRS, outbox/inbox, ML.

Доменные решения: [docs/domain/event-storming.md](docs/domain/event-storming.md). Архитектурные решения: [docs/adr](docs/adr).

## Структура

```
ubi-insurance/
├── build-logic/                 # convention-плагины Gradle (included build)
├── gradle/libs.versions.toml    # все версии — только здесь
├── platform/
│   ├── main/                    # BOM: Spring Boot 4.1 + Spring AI
│   └── gateway/                 # BOM: Spring Boot 4.0 + Spring Cloud (ADR-0003)
├── contracts/                   # protobuf: gRPC-сервисы и интеграционные события
├── libs/
│   ├── messaging/               # transactional outbox + inbox (технический стартер)
│   └── test-support/            # Testcontainers с @ServiceConnection
├── services/
│   ├── gateway/                 # edge: маршрутизация, JWT, rate limit   (Java, WebFlux)
│   ├── policy/                  # котировки, полисы, event sourcing      (Java)
│   ├── rating/                  # тарификация, Kotlin DSL, gRPC          (Kotlin)
│   ├── telematics/              # приём потока, поездки, скоринг         (Java, Kafka Streams)
│   ├── claims/                  # убытки, сага-оркестратор, Spring AI    (Java)
│   └── billing/                 # счета, выплаты, леджер                 (Java)
├── tools/
│   ├── vehicle-simulator/       # парк машин, шлёт телеметрию по gRPC    (Kotlin)
│   └── payment-stub/            # имитация платёжного провайдера
├── infra/local/                 # docker compose: Postgres, Kafka, Redis, Keycloak, ClickHouse
└── docs/                        # event storming, ADR
```

Внутри каждого сервиса: `domain` (чистая модель) → `application` (сценарии, порты, транзакции) →
`adapter.in` / `adapter.out`. Направление зависимостей проверяет `ArchitectureTest`.

## Правила модулей

1. Сервисы не зависят друг от друга в Gradle. Общение — только через `contracts` (gRPC, Kafka).
2. В `libs` нет доменного кода.
3. Каждый модуль явно объявляет платформу: `implementation(platform(projects.platform.main))`.
4. Версии — только в `libs.versions.toml`; то, что управляет Spring Boot BOM, туда не дублируется.

## Порты

| Компонент | HTTP | gRPC |
|---|---|---|
| gateway | 8080 | — |
| policy | 8081 | — |
| rating | 8082 | 9082 |
| telematics | 8083 | 9083 |
| claims | 8084 | — |
| billing | 8085 | — |
| payment-stub | 8090 | — |
| vehicle-simulator | 8091 | — |
| Keycloak | 8180 | — |
| Kafka UI (profile tools) | 8089 | — |

## Запуск

Нужны JDK 21+ для запуска Gradle (JDK 25 для компиляции скачается автоматически через toolchain) и Docker.

```bash
docker compose -f infra/local/compose.yaml up -d
./gradlew build
./gradlew :services:policy:bootRun
```

Токен для ручных запросов (только для локальной разработки, в проде password grant не используется):

```bash
curl -s http://localhost:8180/realms/ubi/protocol/openid-connect/token \
  -d grant_type=password -d client_id=ubi-dev-client -d username=alice -d password=alice
```

Пользователи realm `ubi`: `alice` (policyholder), `bob` (adjuster), `carol` (underwriter).

## Статус

Этап 0 — каркас. Доменной логики пока нет.
