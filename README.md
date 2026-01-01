[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=buildspace-run_demo-microservices-crypto-exchange-reader&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=buildspace-run_demo-microservices-crypto-exchange-reader)

# Crypto Exchange Reader Microservice

A Spring Boot microservice that connects to Binance WebSocket API to receive real-time cryptocurrency prices and
publishes them to RabbitMQ. Built following **Hexagonal Architecture** principles.

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

- **Real-time price streaming** from Binance via WebSocket
- **Dynamic subscription management** via RabbitMQ commands
- **Subscription persistence** in PostgreSQL
- **Observability** with Micrometer/Prometheus metrics
- **Resilience** with retry mechanisms (Resilience4j)
- **Hexagonal Architecture** for clean separation of concerns

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Running Locally

1. **Start infrastructure services:**
   ```bash
   docker-compose up -d
   ```

2. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Check health:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Environment Variables

The application supports the following environment variables for configuration:

| Variable      | Description              | Default  |
|---------------|--------------------------|----------|
| `DB_USERNAME` | PostgreSQL username      | `myuser` |
| `DB_PASSWORD` | PostgreSQL password      | `secret` |

Example:
```bash
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
./mvnw spring-boot:run
```

### Running Tests

**Unit tests only (fast):**
```bash
./mvnw test
```

**Integration tests only (with Testcontainers):**
```bash
./mvnw test -Pintegration
```

**All tests:**
```bash
./mvnw test -Punit-tests,integration
```

> **Note:** Integration tests use Testcontainers for PostgreSQL, RabbitMQ, and MockWebServer for Binance WebSocket simulation. Tests run concurrently for faster execution.

## 📊 Observability

### Available Metrics

| Metric                         | Type    | Description                                |
|--------------------------------|---------|--------------------------------------------|
| `binance.websocket.status`     | Gauge   | WebSocket connection status (1=UP, 0=DOWN) |
| `binance.websocket.silence`    | Gauge   | Time since last message (seconds)          |
| `websocket.messages.processed` | Counter | Total messages processed                   |
| `subscriptions.active`         | Gauge   | Active subscriptions in database           |

### Endpoints

- **Health:** `GET /actuator/health`
- **Prometheus:** `GET /actuator/prometheus`

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.4
- **Messaging:** RabbitMQ with Spring AMQP
- **Database:** PostgreSQL with Spring Data JPA
- **WebSocket:** Spring WebSocket + Tyrus Client
- **Observability:** Micrometer + Prometheus
- **Resilience:** Resilience4j
- **Testing:** JUnit 5 + Mockito + Testcontainers + Awaitility + MockWebServer

## 📁 Project Structure

```
src/main/java/run/buildspace/cryptoreader/
├── domain/
│   ├── model/          # Domain entities
│   └── exception/      # Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/         # Input ports (use cases)
│   │   └── out/        # Output ports (driven)
│   └── service/        # Application services
└── infrastructure/
    ├── adapter/
    │   ├── in/         # Driving adapters
    │   └── out/        # Driven adapters
    └── config/         # Configuration classes
```

## 🎯 Domain Model

### Key Entities

- **`PriceUpdate`**: Represents a real-time price update from Binance
  - `symbol`: Cryptocurrency symbol (e.g., "BTCUSDT")
  - `price`: Current price
  - `timestamp`: Event timestamp

- **`Subscription`**: Represents a user's subscription to a cryptocurrency
  - `symbol`: Cryptocurrency symbol
  - `subscribe`: Boolean flag (true=subscribe, false=unsubscribe)

### Test Coverage

- **Unit Tests**: Test individual components in isolation (services, adapters)
- **Integration Tests**: End-to-end tests with real infrastructure using Testcontainers
  - Database persistence verification
  - RabbitMQ message processing
  - WebSocket connection simulation
  - Concurrent test execution for performance

## 📝 License

This project is for educational/demo purposes.
