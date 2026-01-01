package run.buildspace.crypto.price.reader.application.port.in;

import run.buildspace.crypto.price.reader.domain.model.PendingSubscription;


public interface ForSubscriptionUpdate {
    void handleSubscription(PendingSubscription subscription);

    void reloadAllSubscriptions();

}
