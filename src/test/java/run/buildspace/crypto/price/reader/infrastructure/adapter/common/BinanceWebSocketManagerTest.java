package run.buildspace.crypto.price.reader.infrastructure.adapter.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import run.buildspace.crypto.price.reader.domain.exception.WSException;
import run.buildspace.crypto.price.reader.infrastructure.config.BinanceWebSocketProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;


@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class BinanceWebSocketManagerTest {


    private BinanceWebSocketManager binanceWebSocketManager;


    private BinanceWebSocketProperties properties;

    private final Mocks.WebSocketClientTest webSocketClient = new Mocks.WebSocketClientTest();

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        properties = Mocks.properties();
        meterRegistry = new SimpleMeterRegistry();
        binanceWebSocketManager = new BinanceWebSocketManager(properties, meterRegistry);
        ReflectionTestUtils.setField(binanceWebSocketManager, "client", webSocketClient);

        binanceWebSocketManager.connect();
    }

    @Test
    void socketConnectionCreatedTest() {
        // Given: connection established in @BeforeEach

        // When / Then
        assertEquals(webSocketClient.getSession(), binanceWebSocketManager.getSocket());
        assertNotNull(meterRegistry.find(properties.observability().webSocketStatusGauge()).gauge());
        assertEquals(1.0, meterRegistry.find(properties.observability().webSocketStatusGauge()).gauge().value());
        assertNotNull(meterRegistry.find(properties.observability().webSocketSilenceGauge()).gauge());
    }

    @Test
    void socketConnectionCloseTest() {
        // Given: connection established in @BeforeEach
        // When / Then
        webSocketClient.close();
        assertEquals(0.0, meterRegistry.find(properties.observability().webSocketStatusGauge()).gauge().value());
    }

    @Test
    void handleValidMessageTest() throws InterruptedException {

        // Given
        CountDownLatch latch = new CountDownLatch(1);
        binanceWebSocketManager.getPriceUpdates().subscribe(
                price -> {
                    assertEquals(Mocks.SYMBOL, price.symbol());
                    assertEquals(0, Mocks.PRICE.compareTo(price.price()));
                    latch.countDown();
                });

        // When
        webSocketClient.handleMessage(Mocks.validMessage());

        assertEquals(1, meterRegistry.find(properties.observability().webSocketMessagesProcessedCounter()).counter().count());
        // Then
        latch.await();
    }

    @Test
    void handleConnectionMessageTest() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        binanceWebSocketManager.getPriceUpdates().subscribe(price -> latch.countDown());

        // When
        webSocketClient.handleMessage(Mocks.connectionMessage());

        // Then
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS), "No messages received as expected");
    }

    @Test
    void handleWrongMessageTest() {
        // Given
        binanceWebSocketManager.getPriceUpdates().subscribe(
                price -> fail("No price should be received"),
                error -> {
                    // Then
                    WSException exception = (WSException) error;
                    assertEquals("Invalid message received from Binance: {}", exception.getMessage());
                });

        // When
        webSocketClient.handleMessage(Mocks.wrongMessage());
    }

    @Test
    void interruptExceptionHandleTest() throws InterruptedException, ExecutionException {
        // Given
        WebSocketClient mockSocketClient = Mockito.mock(WebSocketClient.class);
        CompletableFuture<WebSocketSession> mockFuture = Mockito.mock(CompletableFuture.class);

        binanceWebSocketManager = new BinanceWebSocketManager(properties, meterRegistry);
        ReflectionTestUtils.setField(binanceWebSocketManager, "client", mockSocketClient);

        given(mockSocketClient.execute(any(TextWebSocketHandler.class), any(String.class))).willReturn(mockFuture);
        willThrow(new InterruptedException("Test exception")).given(mockFuture).get();

        // When / Then
        WSException wse = assertThrows(WSException.class, () -> binanceWebSocketManager.connect());
        assertEquals("Error connecting to Binance - interrupted: Test exception", wse.getMessage());
    }


    private static class Mocks {
        private Mocks() {
        }

        private static final String SYMBOL = "BTCUSDT";

        private static final String PRICE_TEXT = "12345.67";
        private static final BigDecimal PRICE = new BigDecimal(PRICE_TEXT);

        private static TextMessage validMessage() {
            return new TextMessage("{\"s\":\"" + SYMBOL + "\",\"p\":" + PRICE_TEXT + "}");
        }

        private static TextMessage connectionMessage() {
            return new TextMessage("{\"result\":null,\"id\":5}");
        }

        private static TextMessage wrongMessage() {
            return new TextMessage("{}");
        }

        private static BinanceWebSocketProperties properties() {
            return BinanceWebSocketProperties.builder()
                    .url("wss://fstream.binance.test.com/ws")
                    .retryMaxAttemps(2)
                    .retryInterval(500)
                    .retryIntervalMultiplier(2)
                    .observability(BinanceWebSocketProperties.Observability.builder()
                            .webSocketStatusGauge("web.socket.gauge")
                            .webSocketStatusDescription("web.socket.gauge.description")
                            .webSocketSilenceGauge("web.socket.silence.gauge")
                            .webSocketSilenceDescription("web.socket.silence.gauge.description")
                            .webSocketMessagesProcessedCounter("web.socket.messages.processed")
                            .webSocketMessagesIgnoredCounter("web.socket.messages.ignored")
                            .build())
                    .build();
        }

        private static class WebSocketClientTest implements WebSocketClient {

            private WebSocketHandler webSocketHandler;
            @Getter
            private WebSocketSession session;


            @Override
            public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {
                session = Mockito.mock(WebSocketSession.class);
                this.webSocketHandler = webSocketHandler;
                try {
                    this.webSocketHandler.afterConnectionEstablished(session);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                CompletableFuture<WebSocketSession> future = new CompletableFuture<>();
                future.complete(session);
                return future;
            }

            @Override
            public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, WebSocketHttpHeaders headers, URI uri) {
                throw new UnsupportedOperationException("Method not implemented");
            }

            public void handleMessage(TextMessage message) {
                try {
                    webSocketHandler.handleMessage(session, message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public void close() {
                try {
                    webSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }
}