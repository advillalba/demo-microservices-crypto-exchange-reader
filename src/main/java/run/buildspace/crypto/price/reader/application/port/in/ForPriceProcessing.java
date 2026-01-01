package run.buildspace.crypto.price.reader.application.port.in;

import run.buildspace.crypto.price.reader.domain.model.PriceUpdate;


public interface ForPriceProcessing {
    void processPrice(PriceUpdate priceUpdate);

    void processError(String message);
}
