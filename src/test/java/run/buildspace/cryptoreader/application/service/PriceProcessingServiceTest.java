package run.buildspace.cryptoreader.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.buildspace.cryptoreader.application.port.out.PriceEventPublisher;
import run.buildspace.cryptoreader.domain.model.PriceUpdate;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class PriceProcessingServiceTest {

    @Mock
    private PriceEventPublisher priceEventPublisher;

    @InjectMocks
    private PriceProcessingService priceProcessingService;

    @Test
    void processPriceTest() {
        PriceUpdate priceUpdate = Mocks.priceUpdate();
        priceProcessingService.processPrice(priceUpdate);
        then(priceEventPublisher).should(times(1)).publish(priceUpdate);
    }

    @Test
    void testProcess() {
        priceProcessingService.processError(Mocks.ERROR_MESSAGE);
        then(priceEventPublisher).should(times(1)).publishError(Mocks.ERROR_MESSAGE);
    }



    private static class Mocks {
        private Mocks() {
        }

        static final String SYMBOL = "BTC";
        static final BigDecimal PRICE = BigDecimal.valueOf(30000);
        static final String ERROR_MESSAGE = "Error processing price";

        static PriceUpdate priceUpdate() {
            return PriceUpdate.builder().symbol(SYMBOL).price(PRICE).timestamp(System.currentTimeMillis()).build();
        }
    }
}