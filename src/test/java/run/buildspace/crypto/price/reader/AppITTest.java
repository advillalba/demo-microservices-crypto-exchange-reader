package run.buildspace.crypto.price.reader;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import run.buildspace.crypto.price.reader.domain.model.Subscription;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end integration tests using Testcontainers and MockWebServer.
 * Tests verify the complete application flow from message reception to data persistence.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Execution(ExecutionMode.CONCURRENT)
class AppITTest {

    private static final String FIND_CURRENCY_SYMBOL = "SELECT COUNT(*) FROM subscriptions WHERE symbol = ?";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("test").withUsername("test").withPassword("test");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3-management-alpine");


    private final RabbitTemplate rabbitTemplate;

    private final JdbcTemplate jdbcTemplate;

    @Value("${rabbitmq.subscription-queue}")
    private String subscriptionQueue;

    private static final MockWebServer mockWebServer = new MockWebServer();
    @Autowired
    AppITTest(RabbitTemplate rabbitTemplate, JdbcTemplate jdbcTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }


    @DynamicPropertySource
    static void configureDynamicProperties(DynamicPropertyRegistry registry) throws IOException {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);


        mockWebServer.start();
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new Mocks.BinanceMockWebSocketListener()));

        String mockBinanceUrl = "ws://localhost:" + mockWebServer.getPort() + "/ws";
        registry.add("binance.url", () -> mockBinanceUrl);
    }

    @Test
    void loadApplicationContextTest() {

        assertNotNull(rabbitTemplate, "RabbitTemplate should be initialized");
        assertNotNull(jdbcTemplate, "JdbcTemplate should be initialized");

        assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class), "Database should be accessible");
    }

    @Test
    void subscriptionReceivedAndPersistedTest() {
        // given

        Subscription subscription = Subscription.builder().subscribe(true).symbol(Mocks.BITCOIN).build();

        // when
        rabbitTemplate.convertAndSend(subscriptionQueue, subscription);

        // then
        Awaitility.await().atMost(5, SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            Integer count = jdbcTemplate.queryForObject(FIND_CURRENCY_SYMBOL, Integer.class, Mocks.BITCOIN);
            assertEquals(1, count, "Subscription should be persisted in database");
        });
    }

    @Test
    void handleDuplicateSubscriptionTest() {
        // given
        Subscription ltcSubscription = Subscription.builder().subscribe(true).symbol(Mocks.LITECOIN).build();


        // when
        rabbitTemplate.convertAndSend(subscriptionQueue, ltcSubscription);
        rabbitTemplate.convertAndSend(subscriptionQueue, ltcSubscription);

        // then
        Awaitility.await().atMost(5, SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            Integer btcCount = jdbcTemplate.queryForObject(FIND_CURRENCY_SYMBOL, Integer.class, Mocks.LITECOIN);
            assertEquals(1, btcCount, Mocks.LITECOIN + " subscription should be persisted");
        });
    }

    @Test
    void unsubscriptionReceivedTest() {
        // given
        Subscription ethSubscription = Subscription.builder().subscribe(true).symbol(Mocks.ETHEREUM).build();
        Subscription ethUnsubscription = Subscription.builder().subscribe(false).symbol(Mocks.ETHEREUM).build();


        // when
        rabbitTemplate.convertAndSend(subscriptionQueue, ethSubscription);
        rabbitTemplate.convertAndSend(subscriptionQueue, ethUnsubscription);

        // then
        Awaitility.await().atMost(5, SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            Integer btcCount = jdbcTemplate.queryForObject(FIND_CURRENCY_SYMBOL, Integer.class, Mocks.ETHEREUM);
            assertEquals(0, btcCount, Mocks.ETHEREUM + " subscription should be removed");
        });
    }



    private static class Mocks {
        private Mocks() {
        }

        private static final String BITCOIN = "BTC";
        private static final String ETHEREUM = "ETH";
        private static final String LITECOIN = "LTC";


        private static class BinanceMockWebSocketListener extends WebSocketListener {

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                System.out.println("Mock WebSocket connection opened");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                System.out.println("Mock WebSocket received message: " + text);

                // Send subscription confirmation for any subscription request
                if (text.contains("SUBSCRIBE") || text.contains("subscribe")) {
                    String confirmation = "{\"result\":null,\"id\":1}";
                    webSocket.send(confirmation);
                }
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                System.out.println("Mock WebSocket closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                System.err.println("Mock WebSocket failure: " + t.getMessage());
            }
        }
    }
}