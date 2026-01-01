package run.buildspace.crypto.price.reader.application.service;

import run.buildspace.crypto.price.reader.application.port.out.CryptoStreamSubscriber;
import run.buildspace.crypto.price.reader.application.port.out.PriceSubscriptionRepository;
import run.buildspace.crypto.price.reader.domain.model.PendingSubscription;
import run.buildspace.crypto.price.reader.domain.model.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class BinanceSubscriptionServiceTest {

    @Mock
    private CryptoStreamSubscriber cryptoStreamSubscriber;

    @Mock
    private PriceSubscriptionRepository priceSubscriptionRepository;

    @Spy
    private TransactionTemplate transactionTemplate = new TransactionTemplate(){
        @Override
        public void executeWithoutResult(Consumer<TransactionStatus> action){
            action.accept(null);
        }
    };


    @InjectMocks
    private SubscriptionService subscriptionService;

    private ArgumentCaptor<String> symbolCaptor;

    @BeforeEach
    void setUp() {

        symbolCaptor = ArgumentCaptor.forClass(String.class);

    }

    @Test
    void subscribeTest() {
        //given
        given(priceSubscriptionRepository.exists(Mocks.SYMBOL)).willReturn(false);

        willAnswer(answer -> {
            Consumer<Boolean> callback = answer.getArgument(1);
            callback.accept(true);
            return null;
        }).given(cryptoStreamSubscriber).subscribe(symbolCaptor.capture(), any());
        //when
        subscriptionService.handleSubscription(Mocks.success(true));
        //then
        then(priceSubscriptionRepository).should().save(Mocks.success(true).subscription());
        assertEquals(Mocks.SYMBOL, symbolCaptor.getValue());

    }
    @Test
    void unsubscribeTest() {
        //given
        given(priceSubscriptionRepository.exists(Mocks.SYMBOL)).willReturn(true);
        willAnswer(answer -> {
            Consumer<Boolean> callback = answer.getArgument(1);
            callback.accept(true);
            return null;
        }).given(cryptoStreamSubscriber).unsubscribe(symbolCaptor.capture(), any());
        //when
        subscriptionService.handleSubscription(Mocks.success(false));
        //then
        then(priceSubscriptionRepository).should().save(Mocks.success(false).subscription());
        assertEquals(Mocks.SYMBOL, symbolCaptor.getValue());

    }
    @Test
    void subscribeAlreadyExistsTest() {
        //given
        given(priceSubscriptionRepository.exists(Mocks.SYMBOL)).willReturn(true);
        //when
        subscriptionService.handleSubscription(Mocks.success(true));
        //then
        then(cryptoStreamSubscriber).shouldHaveNoInteractions();

    }

    @Test
    void subscribeErrorTest() {
        //given
        given(priceSubscriptionRepository.exists(Mocks.SYMBOL)).willReturn(false);
        willAnswer(answer -> {
            Consumer<Boolean> callback = answer.getArgument(1);
            callback.accept(false);
            return null;
        }).given(cryptoStreamSubscriber).subscribe(symbolCaptor.capture(), any());
        //when
        subscriptionService.handleSubscription(Mocks.error());
        //then
        assertEquals(Mocks.SYMBOL, symbolCaptor.getValue());
    }

    @Test
    void unsubscribeAlreadyExistsTest() {
        //given
        given(priceSubscriptionRepository.exists(Mocks.SYMBOL)).willReturn(false);
        //when
        subscriptionService.handleSubscription(Mocks.success(false));
        //then
        then(cryptoStreamSubscriber).shouldHaveNoInteractions();

    }
    @Test
    void relloadAllTest() {
        //given
        given(priceSubscriptionRepository.findAll()).willReturn(Mocks.symbols());
        willAnswer(answer -> {
            Consumer<Boolean> callback = answer.getArgument(1);
            callback.accept(true);
            return null;
        }).given(cryptoStreamSubscriber).subscribe(symbolCaptor.capture(), any());
        //when
        subscriptionService.reloadAllSubscriptions();

        //then
        assertEquals(Mocks.SYMBOL, symbolCaptor.getValue());
    }

    @Test
    void relloadAllErrorTest() {
        //given
        given(priceSubscriptionRepository.findAll()).willReturn(Mocks.symbols());
        willAnswer(answer -> {
            Consumer<Boolean> callback = answer.getArgument(1);
            callback.accept(false);
            return null;
        }).given(cryptoStreamSubscriber).subscribe(symbolCaptor.capture(), any());
        //when
        subscriptionService.reloadAllSubscriptions();

        //then
        assertEquals(Mocks.SYMBOL, symbolCaptor.getValue());
    }

    private static class Mocks {
        private Mocks() {
        }

        static final String SYMBOL = "BTC";

        private static PendingSubscription success(boolean subscribe) {
            return PendingSubscription.builder()
                    .subscription(Subscription.builder().symbol(SYMBOL).subscribe(subscribe).build())
                    .onSuccess(() -> assertTrue(true))
                    .onFailure(Assertions::fail)
                    .build();

        }

        private static PendingSubscription error() {
            return PendingSubscription.builder()
                    .subscription(Subscription.builder().symbol(SYMBOL).subscribe(true).build())
                    .onSuccess(Assertions::fail)
                    .onFailure(() -> assertTrue(true))
                    .build();

        }

        private static List<String> symbols() {
            return List.of(SYMBOL);
        }


    }
}