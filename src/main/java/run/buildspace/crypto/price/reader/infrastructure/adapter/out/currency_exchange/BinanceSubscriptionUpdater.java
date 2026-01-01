package run.buildspace.crypto.price.reader.infrastructure.adapter.out.currency_exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import run.buildspace.crypto.price.reader.application.port.out.CryptoStreamSubscriber;
import run.buildspace.crypto.price.reader.domain.exception.BinanceSubscriptionException;
import run.buildspace.crypto.price.reader.infrastructure.adapter.common.BinanceWebSocketManager;
import run.buildspace.crypto.price.reader.infrastructure.adapter.in.crypto_exchange.dto.BinanceSubscription;
import run.buildspace.crypto.price.reader.infrastructure.adapter.in.crypto_exchange.dto.SubscriptionType;
import run.buildspace.crypto.price.reader.infrastructure.adapter.out.currency_exchange.dto.BinancePendingSubscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Component
public class BinanceSubscriptionUpdater implements CryptoStreamSubscriber {
    private final Logger logger = LoggerFactory.getLogger(BinanceSubscriptionUpdater.class);
    private final BinanceWebSocketManager binanceWebSocketManager;
    private final BlockingQueue<BinancePendingSubscription> subscriptionsPool = new LinkedBlockingQueue<>();
    private final BlockingQueue<BinancePendingSubscription> unsubscriptionsPool = new LinkedBlockingQueue<>();

    private static final Integer CURRENCIES_PER_REQUEST = 50;

    @Autowired
    public BinanceSubscriptionUpdater(BinanceWebSocketManager binanceWebSocketManager) {
        this.binanceWebSocketManager = binanceWebSocketManager;
    }

    @Override
    public void subscribe(String symbol, Consumer<Boolean> onComplete) {
        logger.info("Requesting subscription for symbol: {}", symbol);
        subscriptionsPool.add(new BinancePendingSubscription(symbol, onComplete));
    }

    @Override
    public void unsubscribe(String symbol, Consumer<Boolean> onComplete) {
        logger.info("Requesting unsubscription for symbol: {}", symbol);
        unsubscriptionsPool.add(new BinancePendingSubscription(symbol, onComplete));
    }

    @Scheduled(fixedRate = 1200)
    protected void manageSubscriptions() {
        updateSubscriptions(subscriptionsPool, SubscriptionType.SUBSCRIBE);
        updateSubscriptions(unsubscriptionsPool, SubscriptionType.UNSUBSCRIBE);

    }

    private void updateSubscriptions(BlockingQueue<BinancePendingSubscription> subscriptionsPool, SubscriptionType subscribe) {
        List<BinancePendingSubscription> pendingRequests = new ArrayList<>();
        subscriptionsPool.drainTo(pendingRequests, CURRENCIES_PER_REQUEST);
        if (!pendingRequests.isEmpty()) {
            BinanceSubscription.BinanceSubscriptionBuilder request = BinanceSubscription.builder();
            request.method(subscribe);
            pendingRequests.forEach(pendingRequest -> request.param(pendingRequest.symbol().toLowerCase() + "usdt@markPrice@1s"));
            handleSubscription(request.build(), pendingRequests);
        }
    }


    private void handleSubscription(BinanceSubscription subscription, List<BinancePendingSubscription> pendingRequests) {
        if (socketIsOpen()) {
            try {
                binanceWebSocketManager.getSocket().sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(subscription)));
                pendingRequests.forEach(it -> it.callback().accept(true));
            } catch (IOException e) {
                pendingRequests.forEach(it -> it.callback().accept(false));
                throw new BinanceSubscriptionException("Error handling subscription: " + e.getMessage());
            }
        } else {
            pendingRequests.forEach(it -> it.callback().accept(false));
        }
    }

    private boolean socketIsOpen() {
        return binanceWebSocketManager.getSocket() != null && binanceWebSocketManager.getSocket().isOpen();
    }
}
