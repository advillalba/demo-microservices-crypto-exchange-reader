package run.buildspace.cryptoreader.infrastructure.adapter.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import run.buildspace.cryptoreader.domain.exception.WSException;
import run.buildspace.cryptoreader.domain.model.PriceUpdate;
import run.buildspace.cryptoreader.infrastructure.config.BinanceWebSocketProperties;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@EnableConfigurationProperties(BinanceWebSocketProperties.class)
public class BinanceWebSocketManager {
    private final Logger logger = LoggerFactory.getLogger(BinanceWebSocketManager.class);

    private final WebSocketClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private final Sinks.Many<PriceUpdate> priceUpdateSink = Sinks.many().multicast().directBestEffort();

    private final BinanceWebSocketProperties properties;
    private final MeterRegistry meterRegistry;


    private final AtomicInteger connectionStatus = new AtomicInteger(0);

    private final AtomicLong lastMessageTimestamp = new AtomicLong(System.currentTimeMillis());

    @Getter
    private WebSocketSession socket;

    @Getter
    private final Flux<PriceUpdate> priceUpdates = priceUpdateSink.asFlux();

    @Autowired
    public BinanceWebSocketManager(BinanceWebSocketProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.client = new StandardWebSocketClient();

        initMetrics();
    }

    private void initMetrics() {
        Gauge.builder(properties.observability().webSocketStatusGauge(), connectionStatus, AtomicInteger::get).description(properties.observability().webSocketStatusDescription()).register(meterRegistry);

        Gauge.builder(properties.observability().webSocketSilenceGauge(), lastMessageTimestamp, ts -> (System.currentTimeMillis() - ts.get()) / 1000.0)
                .description(properties.observability().webSocketSilenceDescription()).register(meterRegistry);
    }

    @PostConstruct
    public void connect() {
        logger.info("Connecting to Binance...");
        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(properties.retryMaxAttemps()).intervalFunction(IntervalFunction.ofExponentialBackoff(properties.retryInterval(), properties.retryIntervalMultiplier())).build();

        Retry retry = Retry.of("Binance", retryConfig);

        Retry.decorateRunnable(retry, this::executeConnection).run();
    }

    private void executeConnection() {
        try {
            client.execute(new BinanceHandler(), new URI(this.properties.url()).toString()).get();
        } catch (InterruptedException e) {
            connectionStatus.set(0);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new WSException("Error connecting to Binance - interrupted: " + e.getMessage(), e);
        } catch (URISyntaxException | ExecutionException e) {
            connectionStatus.set(0);
            throw new WSException("Error connecting to Binance: " + e.getMessage(), e);
        }
    }


    private class BinanceHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            socket = session;
            connectionStatus.set(1);
            lastMessageTimestamp.set(System.currentTimeMillis());
            logger.info("Connected to Binance. Session ID: {}", session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
            lastMessageTimestamp.set(System.currentTimeMillis());

            logger.debug("Received message: {}", message.getPayload());


            JsonNode node = objectMapper.readTree(message.getPayload());

            if (node.has("s") && node.has("p")) {
                meterRegistry.counter(properties.observability().webSocketMessagesProcessedCounter()).increment();
                priceUpdateSink.tryEmitNext(PriceUpdate.builder().symbol(node.get("s").asText()).price(new BigDecimal(node.get("p").asText())).build());
            } else if (message.getPayload().contains("\"result\":null")) {
                logger.info("Subscription confirmation received");
            } else {
                meterRegistry.counter(properties.observability().webSocketMessagesIgnoredCounter()).increment();
                priceUpdateSink.tryEmitError(new WSException("Invalid message received from Binance: " + message.getPayload()));
            }

        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            connectionStatus.set(0);
            logger.warn("Binance connection closed: {}", status);
        }

    }
}