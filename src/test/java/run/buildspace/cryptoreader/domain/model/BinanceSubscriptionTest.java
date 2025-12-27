package run.buildspace.cryptoreader.domain.model;

import run.buildspace.cryptoreader.domain.exception.InvalidSymbolException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinanceSubscriptionTest {

    @Test
    void subscriptionCreationTest() {
        Subscription subscription = Mocks.subscription();
        assertEquals(Mocks.SYMBOL, subscription.symbol());
        assertTrue(subscription.subscribe());
    }

    @Test
    void subscriptionCreationInvalidSymbolTest() {
        InvalidSymbolException exception = assertThrows(InvalidSymbolException.class, Mocks::subscriptionWrongSymbol);
        assertEquals("Symbol cannot be empty", exception.getMessage());
    }

    private static class Mocks {
        private Mocks() {
        }

        static final String SYMBOL = "BTC";

        static Subscription subscription() {
            return Subscription.builder()
                    .symbol(SYMBOL)
                    .subscribe(true)
                    .build();
        }

        static void subscriptionWrongSymbol() {
            Subscription.builder()
                    .subscribe(true)
                    .build();
        }
    }

}