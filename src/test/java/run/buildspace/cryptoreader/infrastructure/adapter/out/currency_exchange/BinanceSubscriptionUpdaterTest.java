package run.buildspace.cryptoreader.infrastructure.adapter.out.currency_exchange;

import run.buildspace.cryptoreader.domain.exception.BinanceSubscriptionException;
import run.buildspace.cryptoreader.infrastructure.adapter.common.BinanceWebSocketManager;
import run.buildspace.cryptoreader.infrastructure.adapter.in.crypto_exchange.dto.SubscriptionType;
import run.buildspace.cryptoreader.infrastructure.adapter.out.currency_exchange.dto.BinancePendingSubscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class BinanceSubscriptionUpdaterTest {

    @InjectMocks
    private BinanceSubscriptionUpdater binanceSubscriptionUpdater;

    @Mock
    private BinanceWebSocketManager binanceWebSocketManager;

    @Mock
    private WebSocketSession socket;

    private ArgumentCaptor<TextMessage> subscriptionCaptor = ArgumentCaptor.forClass(TextMessage.class);

    private BlockingQueue<BinancePendingSubscription> subscriptionsPool;
    private BlockingQueue<BinancePendingSubscription> unsubscriptionsPool;

    @BeforeEach
    void setUp() {
        this.subscriptionsPool = (BlockingQueue<BinancePendingSubscription>) ReflectionTestUtils
                .getField(binanceSubscriptionUpdater, "subscriptionsPool");
        this.unsubscriptionsPool = (BlockingQueue<BinancePendingSubscription>) ReflectionTestUtils
                .getField(binanceSubscriptionUpdater, "unsubscriptionsPool");
    }

    @Test
    void suscribeTest() throws InterruptedException {
        // when
        binanceSubscriptionUpdater.subscribe(Mocks.CURRENCY, Assertions::assertTrue);
        // then
        BinancePendingSubscription pendingSubscription = subscriptionsPool.poll(1, TimeUnit.SECONDS);
        Assertions.assertNotNull(pendingSubscription);
        assertEquals(Mocks.CURRENCY, pendingSubscription.symbol());
        pendingSubscription.callback().accept(true);
    }

    @Test
    void unsubscribeTest() throws InterruptedException {
        // when
        binanceSubscriptionUpdater.unsubscribe(Mocks.CURRENCY, Assertions::assertTrue);

        // then
        BinancePendingSubscription pendingSubscription = unsubscriptionsPool.poll(1, TimeUnit.SECONDS);
        Assertions.assertNotNull(pendingSubscription);
        assertEquals(Mocks.CURRENCY, pendingSubscription.symbol());
        pendingSubscription.callback().accept(true);
    }

    @Test
    void manageSubscriptionsTest() throws IOException {
        // given
        given(binanceWebSocketManager.getSocket()).willReturn(socket);
        given(socket.isOpen()).willReturn(true);
        willDoNothing().given(socket).sendMessage(subscriptionCaptor.capture());
        subscriptionsPool.add(new BinancePendingSubscription(Mocks.CURRENCY, Assertions::assertTrue));
        unsubscriptionsPool.add(new BinancePendingSubscription(Mocks.CURRENCY, Assertions::assertTrue));

        // when
        binanceSubscriptionUpdater.manageSubscriptions();

        // then
        assertEquals(0, subscriptionsPool.size());
        assertEquals(0, unsubscriptionsPool.size());

        List<TextMessage> messages = subscriptionCaptor.getAllValues();
        assertEquals(2, messages.size());
        assertTrue(messages.get(0).getPayload().contains(SubscriptionType.SUBSCRIBE.name()));
        assertTrue(messages.get(0).getPayload().contains(Mocks.CURRENCY.toLowerCase() + "usdt@markPrice@1s"));
        assertTrue(messages.get(1).getPayload().contains(SubscriptionType.UNSUBSCRIBE.name()));
        assertTrue(messages.get(1).getPayload().contains(Mocks.CURRENCY.toLowerCase() + "usdt@markPrice@1s"));

    }

    @Test
    void manageSubscriptionsNoTest() {
        // when
        binanceSubscriptionUpdater.manageSubscriptions();

        // then
        assertEquals(0, subscriptionsPool.size());
        assertEquals(0, unsubscriptionsPool.size());

        then(socket).shouldHaveNoInteractions();

    }

    @Test
    void socketNotCreatedTest() throws IOException {
        // given
        subscriptionsPool.add(new BinancePendingSubscription(Mocks.CURRENCY, Assertions::assertFalse));
        given(binanceWebSocketManager.getSocket()).willReturn(socket);
        // when
        binanceSubscriptionUpdater.manageSubscriptions();

        // then
        assertEquals(0, subscriptionsPool.size());
        assertEquals(0, unsubscriptionsPool.size());

        then(socket).should(never()).sendMessage(any(TextMessage.class));

    }

    @Test
    void socketNotOpenTest() throws IOException {

        // given
        subscriptionsPool.add(new BinancePendingSubscription(Mocks.CURRENCY, Assertions::assertFalse));
        given(binanceWebSocketManager.getSocket()).willReturn(socket);
        given(socket.isOpen()).willReturn(false);

        // when
        binanceSubscriptionUpdater.manageSubscriptions();

        // then
        assertEquals(0, subscriptionsPool.size());
        assertEquals(0, unsubscriptionsPool.size());

        then(socket).should(never()).sendMessage(any(TextMessage.class));

    }

    @Test
    void interruptedExceptionTest() throws IOException {
        // given
        subscriptionsPool.add(new BinancePendingSubscription(Mocks.CURRENCY, Assertions::assertFalse));
        given(binanceWebSocketManager.getSocket()).willReturn(socket);
        given(socket.isOpen()).willReturn(true);
        willThrow(new IOException("Woops!")).given(socket).sendMessage(any(TextMessage.class));


        // when
        BinanceSubscriptionException bse = assertThrows(BinanceSubscriptionException.class, () -> binanceSubscriptionUpdater.manageSubscriptions());

        // then
        assertEquals("Error handling subscription: Woops!", bse.getMessage());

    }

    private static class Mocks {
        private Mocks() {
        }

        private static final String CURRENCY = "BTC";
    }
}
