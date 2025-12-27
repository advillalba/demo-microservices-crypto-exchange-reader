package run.buildspace.cryptoreader.infrastructure.adapter.in.crypto_exchange;

import run.buildspace.cryptoreader.application.port.in.ForPriceProcessing;
import run.buildspace.cryptoreader.domain.model.Currency;
import run.buildspace.cryptoreader.infrastructure.adapter.common.BinanceWebSocketManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class BinancePriceListenerTest {

    @InjectMocks
    private BinancePriceListener binancePriceListener;
    @Mock
    private BinanceWebSocketManager binanceWebSocketManager;
    @Mock
    private ForPriceProcessing forPriceProcessing;

    private Sinks.Many<Currency> priceSink;

    @BeforeEach
    void setUp() {
        priceSink = Sinks.many().multicast().directBestEffort();
        given(binanceWebSocketManager.getPrices()).willReturn(priceSink.asFlux());
    }

    @Test
    void successMessageTest() throws InterruptedException {
        // given
        Currency currency = Mocks.currency();
        CountDownLatch latch = new CountDownLatch(1);
        
        binancePriceListener.run();
        
        // when
        priceSink.tryEmitNext(currency);
        latch.await(500, TimeUnit.MILLISECONDS);
        
        // then
        then(forPriceProcessing).should().processPrice(currency);
    }

    private static class Mocks{
        private Mocks() {
        }
        private static Currency currency(){
            return new PodamFactoryImpl().manufacturePojo(Currency.class);
        }
    }
}