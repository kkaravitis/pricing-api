![Project Status](https://img.shields.io/badge/status-WIP-yellow.svg)

**Work in Progress**
# Pricing API
**Pricing API** is the intermediate Spring Boot service that connects the demo’s Angular UI with the Flink streaming job.  
It exposes REST endpoints for sending **events** (clicks, orders, inventory, business rules), persists **pricing results** from Kafka, broadcasts them over **WebSocket/STOMP**, and manages a simple **Competitor Prices** CRUD backed by PostgreSQL.

---

## Table of contents

- [Architecture](#architecture)
- [Main features](#main-features)
- [Tech stack](#tech-stack)
- [Project layout](#project-layout)
- [REST API](#rest-api)
    - [Business/behavioral events → Kafka](#businessbehavioral-events--kafka)
    - [Query latest pricing results](#query-latest-pricing-results)
    - [Competitor Prices CRUD](#competitor-prices-crud)
- [WebSocket / STOMP](#websocket--stomp)
- [Kafka Integration](#kafka-integration)
- [Database](#database)
- [Configuration](#configuration)
- [Local development](#local-development)
- [Docker](#docker)
- [docker-compose](#docker-compose)
- [jOOQ code generation & Flyway](#jooq-code-generation--flyway)
- [Testing](#testing)
- [License](#license)

---

## Architecture

```
Angular UI  ──(REST)──►  Pricing API  ──(Kafka)──►  Flink pricing-job
     ▲                           │                        │
     │                           │                        ▼
     └──────(WebSocket/STOMP)◄───┴───(Kafka consumer)◄───pricing-results
                                   │
                                   └──► PostgreSQL (persist latest results & competitor prices)
```

- **Angular UI** posts user events and subscribes to live pricing updates over WebSocket.
- **Pricing API** publishes events to Kafka and consumes `pricing-results`, stores to Postgres, and pushes to WebSocket clients.
- **Flink pricing-job** ingests events, produces `pricing-results` back to Kafka.

---

## Main features

- **Event ingestion** (REST → Kafka): Clicks, Orders, Inventory, Business Rule changes.
- **Pricing Results**:
    - Kafka consumer enriches incoming pricing-results and persists them (idempotent insert).
    - WebSocket/STOMP broadcasting for real-time UI updates.
    - Query endpoints for latest price per product, including simple “price change” labels.
- **Competitor Prices**:
    - Minimal CRUD for competitor prices (simulate external competitor feed used by Flink).
- **Configurable topics & currency** via `application.yml` / environment.

---

## Tech stack

- **Java 21**, **Spring Boot 3.2+** (Web, WebSocket/STOMP, WebFlux client, jOOQ)
- **PostgreSQL**, **Flyway** (migrations), **jOOQ** (type-safe SQL)
- **Apache Kafka** (Spring Kafka) + **Kafdrop** (UI)
- **Docker** & **docker-compose**

---

## Project layout

```
pricing-api/
├─ src/main/java/com/wordpress/kkaravitis/pricing/
│  ├─ PricingApiApplication.java
│  ├─ adapter/inbound/
│  │  ├─ PricingController.java                # REST for events & queries
│  │  ├─ CompetitorPriceController.java        # REST for competitor prices
│  │  └─ PricingResultConsumer.java            # Kafka consumer for pricing-results
│  ├─ adapter/outbound/
│  │  ├─ EventPublisher.java                   # Publishes events to Kafka
│  │  ├─ ProductPriceRepository.java           # Latest price + change queries (jOOQ)
│  │  ├─ PriceResultRepository.java            # Persist pricing results (idempotent)
│  │  ├─ PricingWebSocketPublisher.java        # Push results via STOMP
│  │  └─ TopicsData.java                       # Binds topic names from config
│  ├─ config/
│  │  ├─ KafkaProducerConfig.java
│  │  ├─ WebConfig.java                        # CORS *
│  │  └─ WebSocketConfig.java                  # /ws + /stream
│  ├─ domain/
│  │  ├─ Money.java
│  │  ├─ PriceAdvisorService.java              # Orchestration across adapters/repos
│  │  ├─ PricingResult.java
│  │  ├─ ProductPriceResultWithChange.java
│  │  ├─ events/ (ClickEvent, OrderEvent, InventoryLevelEvent, BusinessRuleEvent, PriceRule)
│  │  └─ competitor/ (Repository/Service/DTOs)
│  └─ ...
├─ src/main/resources/
│  ├─ application.yml                          # topics, datasource, kafka, jooq
│  └─ db/migration/
│     ├─ V1__init.sql                          # product_price_results
│     └─ V2__create_competitor_price.sql       # competitor_price
├─ Dockerfile
├─ docker-compose.yml
├─ pom.xml
└─ README.md
```

\* CORS is relaxed for the demo.

---

## REST API

### Business/behavioral events → Kafka

`PricingController` exposes endpoints to send events that the Flink job consumes.  
(Exact request mappings are designed around the following payloads.)

#### Click event
```http
POST /api/prices/events/click
Content-Type: application/json

{
  "productId": "SKU-001",
  "productName": "Acme Mug"
}
```

#### Order event
```http
POST /api/prices/events/order
Content-Type: application/json

{
  "orderId": "ORD-1001",
  "productId": "SKU-001",
  "productName": "Acme Mug",
  "quantity": 2
}
```

#### Inventory level event
```http
POST /api/prices/events/inventory
Content-Type: application/json

{
  "productId": "SKU-001",
  "productName": "Acme Mug",
  "quantity": 120
}
```

#### Business rule (min/max price) event
```http
POST /api/prices/events/rule
Content-Type: application/json

{
  "productId": "SKU-001",
  "productName": "Acme Mug",
  "min": 8.50,
  "max": 19.99
}
```

All of the above are published to Kafka via `EventPublisher` using topic names from config.

### Query latest pricing results

Endpoints to fetch **latest price records** (per product) with change metadata:

```http
GET /api/prices/latest              # list (latest per product)
GET /api/prices/latest/{productId}  # single product
```

Example response item:
```json
{
  "productId": "SKU-001",
  "productName": "Acme Mug",
  "price": 12.99,
  "currency": "EUR",
  "timestamp": "2025-10-06T21:52:10",
  "previousPrice": 12.49,
  "priceChangePercent": 4.00,
  "priceChangeLabel": "increase",
  "competitorPrice": 11.90,
  "inventoryLevel": 120,
  "modelPrediction": 12.95
}
```

> Change fields are computed via jOOQ window functions in `ProductPriceRepository`.

### Competitor Prices CRUD

Minimal controller used by the demo UI and by Flink’s HTTP client.

- **Create/Update** (upsert):
  ```http
  POST /api/competitor-prices
  Content-Type: application/json

  {
    "productId": "SKU-001",
    "productName": "Acme Mug",
    "competitorPrice": 11.90
  }
  ```
  Returns `200 OK`.

- **Get all**:
  ```http
  GET /api/competitor-prices
  ```
  Response:
  ```json
  [
    {
      "productId": "SKU-001",
      "productName": "Acme Mug",
      "competitorPrice": 11.90,
      "updatedAt": "2025-10-06T21:50:17.382Z"
    }
  ]
  ```

- **Get by product id** (for Flink):
  ```http
  GET /api/competitor-prices/SKU-001
  ```
  Response:
  ```json
  {
    "productId": "SKU-001",
    "price": { "amount": 11.90, "currency": "EUR" }
  }
  ```
  or `404` if missing.

---

## WebSocket / STOMP

- **Handshake endpoint**: `GET /ws` (SockJS enabled for fallback)
- **Simple broker**: prefix `/stream`
- **Destination used**: `/stream/prices/`

Flow:
1. Kafka `pricing-results` message arrives.
2. Persist to DB.
3. Broadcast payload to `/stream/prices/` so the Angular table refreshes live.

Example subscription (JS with `@stomp/stompjs`):
```ts
client.subscribe('/stream/prices/', (msg) => {
  const priceUpdate = JSON.parse(msg.body);
  // update UI table
});
```

---

## Kafka Integration

Topic names are externalized under `app.kafka.topics` in `application.yml` and bound to a simple holder (e.g., `TopicsData`).

```yaml
app:
  currency: EUR
  kafka:
    topics:
      click-event-topic: click-event
      order-event-topic: order-event
      inventory-event-topic: inventory-event
      rule-event-topic: rule-event
      price-result-topic: pricing-results
```

- **Producers**: send events received via REST.
- **Consumer**: listens to `pricing-results`, persists, pushes over WebSocket.

Spring Kafka properties (abridged):
```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: pricing-api-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: '*'
```

---

## Database

### Tables (created by Flyway)

`V1__init.sql` — `product_price_results`
```sql
CREATE TABLE product_price_results (
  id BIGSERIAL PRIMARY KEY,
  product_id VARCHAR(64) NOT NULL,
  product_name VARCHAR(255),
  price NUMERIC(12, 2) NOT NULL,
  currency VARCHAR(3) DEFAULT 'EUR',
  timestamp TIMESTAMP NOT NULL,
  demand_metric DOUBLE PRECISION,
  competitor_price DOUBLE PRECISION,
  inventory_level DOUBLE PRECISION,
  model_prediction DOUBLE PRECISION,
  CONSTRAINT uq_product_time UNIQUE (product_id, timestamp)
);
CREATE INDEX idx_product_time ON product_price_results (product_id, timestamp DESC);
```

`V2__create_competitor_price.sql` — `competitor_price`
```sql
CREATE TABLE IF NOT EXISTS competitor_price (
  id                BIGSERIAL PRIMARY KEY,
  product_id        VARCHAR(128) NOT NULL UNIQUE,
  product_name      VARCHAR(255),
  competitor_price  NUMERIC(10,2) NOT NULL,
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS ix_competitor_price_updated ON competitor_price(updated_at DESC);
```

> jOOQ repositories encapsulate reads/writes and change calculations.

---

## Configuration

Main config: `src/main/resources/application.yml`.

Frequently overridden via env vars:
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/pricing_db}
    username: ${DB_USERNAME:pricing_user}
    password: ${DB_PASSWORD:pricing_pass}

spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

- **Currency** and **topic names**: `app.currency`, `app.kafka.topics.*`
- **CORS** is open for demo (see `WebConfig`).
- **WebSocket** endpoint is `/ws` (SockJS), broker prefix `/stream`.

---

## Local development

### Prerequisites
- JDK 21
- Maven 3.9+
- Docker (optional but recommended for infra)

### Start a local Postgres & Kafka (optional)
Use the provided `docker-compose.yml` (see below) or your own infra.

### Migrations & jOOQ (when needed)
```bash
# Requires DB_* environment variables set to a running Postgres
mvn flyway:migrate
mvn generate-sources   # runs jOOQ codegen against the DB schema
```

### Run the app
```bash
mvn spring-boot:run
# or
mvn -DskipTests package && java -jar target/pricing-api-*.jar
```

Common envs:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/pricing_db
export DB_USERNAME=pricing_user
export DB_PASSWORD=pricing_pass
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## Docker

**Dockerfile** (simplified production image):
```dockerfile
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS=""
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
```

Build & run:
```bash
mvn -DskipTests package
docker build -t pricing-api:0.0.1 .
docker run --rm -p 8080:8080 \
  -e DB_URL='jdbc:postgresql://host.docker.internal:5432/pricing_db' \
  -e DB_USERNAME='pricing_user' \
  -e DB_PASSWORD='pricing_pass' \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS='host.docker.internal:9092' \
  pricing-api:0.0.1
```

Optional tuning:
```bash
-e JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
```

---

## docker-compose

This compose file spins up **Postgres**, **Kafka** (with Zookeeper), **Kafdrop**, and **pricing-api**.

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: pricing_db
      POSTGRES_USER: pricing_user
      POSTGRES_PASSWORD: pricing_pass
    ports: ["5432:5432"]
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U pricing_user -d pricing_db || exit 1"]
      interval: 5s
      timeout: 3s
      retries: 20

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports: ["2181:2181"]

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on: [zookeeper]
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1

  kafdrop:
    image: obsidiandynamics/kafdrop:latest
    depends_on: [kafka]
    ports: ["9001:9001"]
    environment:
      KAFKA_BROKERCONNECT: "kafka:9092"
      JVM_OPTS: "-Xms32M -Xmx64M"
      SERVER_SERVLET_CONTEXTPATH: "/"

  pricing-api:
    image: pricing-api:0.0.1
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_started
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/pricing_db
      DB_USERNAME: pricing_user
      DB_PASSWORD: pricing_pass
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports: ["8080:8080"]

volumes:
  pgdata: {}
```

Usage:
```bash
docker compose up -d
# Kafdrop:  http://localhost:9001
# API:      http://localhost:8080
```

---

## jOOQ code generation & Flyway

- Write your schema changes as Flyway migrations under `src/main/resources/db/migration`.
- Generate jOOQ classes from the **live DB** (post-migration).

Typical flow:
```bash
# 1) Start Postgres (e.g., via docker-compose)
# 2) Migrate schema
mvn flyway:migrate

# 3) Generate jOOQ sources (reads DB schema)
mvn generate-sources
```

> The POM config expects DB connection from environment (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) when running the codegen/migrations.

---

## Testing

Run unit tests:
```bash
mvn test
```

---

## License

This project is licensed under the **Apache License 2.0**.  
See the `LICENSE` file for details.
