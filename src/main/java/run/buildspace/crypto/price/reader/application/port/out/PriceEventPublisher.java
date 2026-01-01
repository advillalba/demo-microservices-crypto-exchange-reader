package run.buildspace.crypto.price.reader.application.port.out;

import run.buildspace.crypto.price.reader.domain.model.PriceUpdate;

public interface PriceEventPublisher {
    void publish(PriceUpdate priceUpdate);

    void publishError(String message);
}
