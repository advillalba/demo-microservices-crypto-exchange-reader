package run.buildspace.crypto.price.reader.domain.model;

import lombok.Builder;

@Builder
public record PendingSubscription(Subscription subscription, Runnable onSuccess, Runnable onFailure) {
}
