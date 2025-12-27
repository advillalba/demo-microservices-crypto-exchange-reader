package run.buildspace.cryptoreader.application.port.in;

import run.buildspace.cryptoreader.domain.model.Currency;


public interface ForPriceProcessing {
    void processPrice(Currency price);

    void processError(String message);
}
