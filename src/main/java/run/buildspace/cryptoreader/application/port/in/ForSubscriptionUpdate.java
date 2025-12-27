package run.buildspace.cryptoreader.application.port.in;

import run.buildspace.cryptoreader.domain.model.PendingSubscription;


public interface ForSubscriptionUpdate {
    void handleSubscription(PendingSubscription subscription);

    void reloadAllSubscriptions();

}
