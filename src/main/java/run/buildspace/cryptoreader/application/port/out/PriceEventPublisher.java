package run.buildspace.cryptoreader.application.port.out;

import run.buildspace.cryptoreader.domain.model.PriceUpdate;

public interface PriceEventPublisher {
    void publish(PriceUpdate priceUpdate);

    void publishError(String message);
}
