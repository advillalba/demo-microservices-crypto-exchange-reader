package run.buildspace.cryptoreader.domain.model;

import lombok.Builder;

@Builder
public record PendingSubscription(Subscription subscription, Runnable onSuccess, Runnable onFailure) {
}
