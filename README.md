[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=buildspace-run_demo-microservices-crypto-exchange-reader&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=buildspace-run_demo-microservices-crypto-exchange-reader)

# Crypto Exchange Reader Microservice

A Spring Boot microservice that connects to **Binance WebSocket API** to receive real-time cryptocurrency price updates and publishes them to **RabbitMQ**. It also handles dynamic subscription management with persistence in **PostgreSQL**. Built following **Hexagonal Architecture** principles.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Infrastructure                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │   Binance   │    │  RabbitMQ   │    │     PostgreSQL      │  │
│  │  WebSocket  │    │   Broker    │    │     Database        │  │
│  └──────┬──────┘    └──────┬──────┘    └──────────┬──────────┘  │
│         │                  │                      │             │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌──────────▼─────────┐   │
│  │   Binance   │    │  RabbitMQ   │    │   Subscription     │   │
│  │  Listener   │    │  Publisher  │    │   Repository       │   │
│  │  (Adapter)  │    │  (Adapter)  │    │   (Adapter)        │   │
│  └──────┬──────┘    └──────▲──────┘    └─────────▲──────────┘   │
└─────────┼──────────────────┼─────────────────────┼──────────────┘
          │                  │                     │
┌─────────▼──────────────────┼─────────────────────┼──────────────┐
│                      Application                 |              │
│  ┌──────────────────┐    ┌─┴────────────────────┐│              │
│  │ PriceProcessing  │    │   Subscription       ││              │
│  │    Service       │────│      Service         │┘              │
│  └──────────────────┘    └──────────────────────┘               │
└─────────────────────────────────────────────────────────────────┘
```

## ✨ Features

- **Real-time price streaming** - Connects to Binance public streams
- **Dynamic subscription management** - Add/remove symbols at runtime
- **Resilient connectivity** - Automatic reconnection strategies with backoff
- **Message reliability** - Dead Letter Queues (DLQ) for failed messages
- **Persistence** - Stores active subscriptions in PostgreSQL
- **Observability** - Custom metrics for WebSocket health and message rates
- **Hexagonal Architecture** - Clean separation of domain and infrastructure

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+
- PostgreSQL 15+
- RabbitMQ 3.12+

### Running Locally

1. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   
   Spring Boot will automatically:
   - Detect and start Docker Compose services (`compose.yml`)
   - Initialize PostgreSQL and RabbitMQ
   - Connect to Binance WebSocket
   - Start processing default subscriptions
   
2. **Check health:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

**Access Services:**
- RabbitMQ Management UI: http://localhost:15672 (credentials: guest/guest)
- PostgreSQL: localhost:5432 (credentials: myuser/secret)

### Environment Variables

The application supports the following environment variables for configuration:

| Variable            | Description                    | Default     |
|---------------------|--------------------------------|-------------|
| `DB_USERNAME`       | PostgreSQL username            | `myuser`    |
| `DB_PASSWORD`       | PostgreSQL password            | `secret`    |
| `RABBITMQ_HOST`     | RabbitMQ broker hostname       | `localhost` |
| `RABBITMQ_PORT`     | RabbitMQ AMQP port             | `5672`      |
| `RABBITMQ_USER`     | RabbitMQ username              | `guest`     |
| `RABBITMQ_PASSWORD` | RabbitMQ password              | `guest`     |
| `RABBITMQ_VHOST`    | RabbitMQ virtual host          | `/`         |

Example:
```bash
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
export RABBITMQ_HOST=rabbitmq.example.com
mvn spring-boot:run
```

### Running Tests

**Unit tests only (fast):**
```bash
mvn test
```

**Integration tests only:**
```bash
mvn test -Psmoke-tests
```

> **Note:** Integration tests use **Testcontainers** to spin up real instances of PostgreSQL and RabbitMQ, and **MockWebServer** to simulate the Binance WebSocket API.

## 📊 Observability

### Available Metrics

Spring Boot Actuator and Micrometer expose the following custom metrics:

| Metric                         | Type    | Description                                |
|--------------------------------|---------|--------------------------------------------|
| `binance.websocket.status`     | Gauge   | WebSocket connection status (1=UP, 0=DOWN) |
| `binance.websocket.silence`    | Gauge   | Time since last message (seconds)          |
| `binance.websocket.messages.processed` | Counter | Total messages processed successfully |
| `binance.websocket.messages.ignored`   | Counter | Messages ignored (e.g. keep-alives)   |
| `subscriptions.active`         | Gauge   | Number of active cryptocurrency subscriptions |

### Endpoints

- **Health:** `GET /actuator/health`
- **Metrics:** `GET /actuator/metrics`
- **Prometheus:** `GET /actuator/prometheus`

### Monitoring Best Practices

**WebSocket Health:**
```promql
# Alert if WebSocket is disconnected for more than 1 minute
avg_over_time(binance_websocket_status[1m]) < 1
```

**Data Stagnation:**
```promql
# Alert if no messages received for 30 seconds
binance_websocket_silence > 30
```

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.4
- **Messaging:** RabbitMQ with Spring AMQP
- **Database:** PostgreSQL with Spring Data JPA
- **WebSocket:** Spring WebSocket + Tyrus Client
- **Observability:** Micrometer + Prometheus
- **Resilience:** Resilience4j (Retry/Circuit Breaker)
- **Testing:** JUnit 5 + Mockito + Testcontainers + Awaitility + MockWebServer
- **Build Tool:** Maven 3.9+

## 📁 Project Structure

```
src/main/java/run/buildspace/crypto/price/reader/
├── domain/
│   ├── model/          # Domain entities (PriceUpdate, Subscription)
│   └── exception/      # Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/         # Input ports (Use Cases)
│   │   └── out/        # Output ports (Repositories, Publishers)
│   └── service/        # Application services (Business Logic)
└── infrastructure/
     ├── adapter/
     │   ├── in/         # Driving adapters (WebSocket Listener)
     │   └── out/        # Driven adapters (RabbitMQ Publisher, Postgres Repo)
     └── config/         # Configuration classes
```

## 🎯 Domain Model

### Key Entities

- **`PriceUpdate`**: Represents a real-time price update
  - `symbol`: Cryptocurrency pair (e.g., "BTC")
  - `price`: Current trading price
  - `timestamp`: Event occurrence time

- **`Subscription`**: Represents a tracked asset
  - `symbol`: Unique identifier for the cryptocurrency pair
  - `subscribe`: Boolean flag indicating subscription state (true = subscribed)

## ⚡ Resilience & Reliability

### Automatic Reconnection

The service is designed to handle network instability:
1. **Connection Loss**: Detects WebSocket closure immediately.
2. **Backoff Strategy**: Attempts reconnection with exponential backoff (starting at 500ms).
3. **State Recovery**: Re-subscribes to all active currencies upon successful reconnection.

### Error Handling

- **Invalid Messages**: Malformed JSON or unknown events are logged and ignored to prevent stream interruption.
- **Publishing Failures**: If RabbitMQ is down, messages may be dropped to prevent memory leaks (dependent on configuration), but critical subscription events are retried.
- **Dead Letter Queue (DLQ)**: Failed messages are routed to `dead-letter-queue` for manual inspection.

## 🔍 Troubleshooting

- **No data received?** Check that subscriptions exist in the database.
- **Connection refused?** Verify Docker containers are running with `docker ps`.


## 🤝 Integration with Consumer

This producer feeds data to the **Crypto Price Persister** (consumer) microservice:

1. **Producer** connects to Binance and forwards `PriceUpdate` events to RabbitMQ.
2. **Consumer** reads queues and stores history in PostgreSQL.

**Data Flow:**
```
Binance API (WS) → [Producer] → RabbitMQ Exchange → [Consumer] → DB
```
