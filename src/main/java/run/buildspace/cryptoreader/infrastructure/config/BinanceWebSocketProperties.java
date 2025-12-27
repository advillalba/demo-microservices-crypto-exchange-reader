package run.buildspace.cryptoreader.infrastructure.config;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Accessors(fluent = true)
@Getter
@ConfigurationProperties(prefix = "binance")
public class BinanceWebSocketProperties {

    private final String url;
    private final int retryMaxAttemps;
    private final int retryInterval;
    private final int retryIntervalMultiplier;
    private final Observability observability;

    @Builder
    @Accessors(fluent = true)
    @Getter
    public static class Observability{
        private final String webSocketStatusGauge;
        private final String webSocketStatusDescription;
        private final String webSocketSilenceGauge;
        private final String webSocketSilenceDescription;
        private final String webSocketMessagesProcessedCounter;
        private final String webSocketMessagesIgnoredCounter;
    }
}
