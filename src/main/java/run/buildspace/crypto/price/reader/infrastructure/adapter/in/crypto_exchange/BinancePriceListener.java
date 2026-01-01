package run.buildspace.crypto.price.reader.infrastructure.adapter.in.crypto_exchange;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.buildspace.crypto.price.reader.application.port.in.ForPriceProcessing;
import run.buildspace.crypto.price.reader.domain.model.PriceUpdate;
import run.buildspace.crypto.price.reader.infrastructure.adapter.common.BinanceWebSocketManager;
import run.buildspace.crypto.price.reader.infrastructure.config.observability.Trace;

@Component
public class BinancePriceListener {
    private final Logger logger = LoggerFactory.getLogger(BinancePriceListener.class);
    private final BinanceWebSocketManager binanceWebSocketManager;
    private final ForPriceProcessing forPriceProcessing;

    @Autowired
    public BinancePriceListener(BinanceWebSocketManager binanceWebSocketManager, ForPriceProcessing forPriceProcessing) {
        this.binanceWebSocketManager = binanceWebSocketManager;
        this.forPriceProcessing = forPriceProcessing;
    }

    @PostConstruct
    public void run() {
        binanceWebSocketManager.getPriceUpdates().subscribe(
                price -> Trace.trace(this::processPrice, price),
                error -> Trace.trace(this::processError, error),
                () -> logger.info("Closed connection with Binance")
        );
    }

    private void processPrice(PriceUpdate priceUpdate) {
        logger.info("Received price update: {}", priceUpdate);
        forPriceProcessing.processPrice(priceUpdate);
    }

    private void processError(Throwable error) {
        logger.error("Error: {}", error.getMessage());
        forPriceProcessing.processError(error.getMessage());
    }


}
