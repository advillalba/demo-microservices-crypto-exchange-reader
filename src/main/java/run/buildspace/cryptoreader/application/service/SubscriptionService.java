package run.buildspace.cryptoreader.application.service;

import run.buildspace.cryptoreader.application.port.in.ForSubscriptionUpdate;
import run.buildspace.cryptoreader.application.port.out.CryptoStreamSubscriber;
import run.buildspace.cryptoreader.application.port.out.PriceSubscriptionRepository;
import run.buildspace.cryptoreader.domain.model.PendingSubscription;
import run.buildspace.cryptoreader.domain.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class SubscriptionService implements ForSubscriptionUpdate {
    private final CryptoStreamSubscriber cryptoStreamSubscriber;
    private final PriceSubscriptionRepository priceSubscriptionRepository;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public SubscriptionService(CryptoStreamSubscriber cryptoStreamSubscriber, PriceSubscriptionRepository priceSubscriptionRepository, TransactionTemplate transactionTemplate) {
        this.cryptoStreamSubscriber = cryptoStreamSubscriber;
        this.priceSubscriptionRepository = priceSubscriptionRepository;
        this.transactionTemplate = transactionTemplate;

    }

    @Override
    public void handleSubscription(PendingSubscription pendingSubscription) {
        boolean exists = priceSubscriptionRepository.exists(pendingSubscription.subscription().symbol());
        if ((exists && pendingSubscription.subscription().subscribe()) || (!exists && !pendingSubscription.subscription().subscribe())) {
            pendingSubscription.onSuccess().run();
        } else {
            Subscription subscription = pendingSubscription.subscription();
            BiConsumer<String, Consumer<Boolean>> action = pendingSubscription.subscription().subscribe() ? cryptoStreamSubscriber::subscribe : cryptoStreamSubscriber::unsubscribe;
            action.accept(subscription.symbol(), success -> {
                if (success) {
                    transactionTemplate.executeWithoutResult(status -> priceSubscriptionRepository.save(subscription));
                    pendingSubscription.onSuccess().run();
                } else {
                    pendingSubscription.onFailure().run();
                }
            });
        }
    }

    @Override
    public void reloadAllSubscriptions() {
        priceSubscriptionRepository.findAll().forEach(it -> {
            cryptoStreamSubscriber.subscribe(it, success -> {
                if (success) {
                    logger.info("Reloaded subscription of {}", it);
                } else {
                    logger.error("Error reloading subscription of {}", it);
                }
            });
        });
    }

}
