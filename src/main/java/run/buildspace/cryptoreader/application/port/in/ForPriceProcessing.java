package run.buildspace.cryptoreader.application.port.in;

import run.buildspace.cryptoreader.domain.model.PriceUpdate;


public interface ForPriceProcessing {
    void processPrice(PriceUpdate priceUpdate);

    void processError(String message);
}
