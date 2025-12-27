package run.buildspace.cryptoreader.infrastructure.adapter.in.crypto_exchange;

import run.buildspace.cryptoreader.application.port.in.ForPriceProcessing;
import run.buildspace.cryptoreader.infrastructure.adapter.common.BinanceWebSocketManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        binanceWebSocketManager.getPrices().subscribe(
                price -> {
                    logger.info("Price received: {}", price);
                    forPriceProcessing.processPrice(price);
                },
                error -> {
                    logger.error("Error: {}", String.valueOf(error));
                    forPriceProcessing.processError(String.valueOf(error));
                },
                () -> logger.info("Closed connection with Binance")
        );
    }


}
