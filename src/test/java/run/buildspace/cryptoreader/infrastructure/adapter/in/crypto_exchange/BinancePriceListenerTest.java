package run.buildspace.cryptoreader.infrastructure.adapter.in.crypto_exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks;
import run.buildspace.cryptoreader.application.port.in.ForPriceProcessing;
import run.buildspace.cryptoreader.domain.model.PriceUpdate;
import run.buildspace.cryptoreader.infrastructure.adapter.common.BinanceWebSocketManager;
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

    private Sinks.Many<PriceUpdate> priceSink;

    @BeforeEach
    void setUp() {
        priceSink = Sinks.many().multicast().directBestEffort();
        given(binanceWebSocketManager.getPriceUpdates()).willReturn(priceSink.asFlux());
    }

    @Test
    void successMessageTest() throws InterruptedException {
        // given
        PriceUpdate priceUpdate = Mocks.priceUpdate();
        CountDownLatch latch = new CountDownLatch(1);
        
        binancePriceListener.run();
        
        // when
        priceSink.tryEmitNext(priceUpdate);
        latch.await(500, TimeUnit.MILLISECONDS);
        
        // then
        then(forPriceProcessing).should().processPrice(priceUpdate);
    }

    private static class Mocks{
        private Mocks() {
        }
        private static PriceUpdate priceUpdate(){
            return new PodamFactoryImpl().manufacturePojo(PriceUpdate.class);
        }
    }
}