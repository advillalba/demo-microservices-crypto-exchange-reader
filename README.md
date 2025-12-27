# Crypto Exchange Reader Microservice

A Spring Boot microservice that connects to Binance WebSocket API to receive real-time cryptocurrency prices and
publishes them to RabbitMQ. Built following **Hexagonal Architecture** principles.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Infrastructure                            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │   Binance   │    │  RabbitMQ   │    │     PostgreSQL      │  │
│  │  WebSocket  │    │   Broker    │    │     Database        │  │
│  └──────┬──────┘    └──────┬──────┘    └──────────┬──────────┘  │
│         │                  │                      │              │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌─────────▼──────────┐   │
│  │   Binance   │    │  RabbitMQ   │    │   Subscription     │   │
│  │  Listener   │    │  Publisher  │    │   Repository       │   │
│  │  (Adapter)  │    │  (Adapter)  │    │   (Adapter)        │   │
│  └──────┬──────┘    └──────▲──────┘    └─────────▲──────────┘   │
└─────────┼──────────────────┼─────────────────────┼──────────────┘
          │                  │                     │
┌─────────▼──────────────────┼─────────────────────┼──────────────┐
│                      Application                                 │
│  ┌──────────────────┐    ┌─┴────────────────────┐│               │
│  │ PriceProcessing  │    │   Subscription       ││               │
│  │    Service       │────│      Service         │┘               │
│  └──────────────────┘    └──────────────────────┘                │
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

### Running Tests

```bash
./mvnw test
```

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
- **Testing:** JUnit 5 + Mockito + Testcontainers

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

## 📝 License

This project is for educational/demo purposes.
