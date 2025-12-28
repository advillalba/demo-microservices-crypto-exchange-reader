package run.buildspace.cryptoreader.application.service;

import run.buildspace.cryptoreader.application.port.in.ForPriceProcessing;
import run.buildspace.cryptoreader.application.port.out.PriceEventPublisher;
import run.buildspace.cryptoreader.domain.model.PriceUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriceProcessingService implements ForPriceProcessing {

    private final PriceEventPublisher priceEventPublisher;


    @Autowired
    public PriceProcessingService(PriceEventPublisher priceEventPublisher) {
        this.priceEventPublisher = priceEventPublisher;
    }

    @Override
    public void processPrice(PriceUpdate priceUpdate) {
        priceEventPublisher.publish(priceUpdate);
    }

    @Override
    public void processError(String error) {
        priceEventPublisher.publishError(error);
    }
}
