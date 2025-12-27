package run.buildspace.cryptoreader.domain.model;

import run.buildspace.cryptoreader.domain.exception.InvalidPriceException;
import run.buildspace.cryptoreader.domain.exception.InvalidSymbolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class CurrencyTest {


    @Test
    void currencyCreationTest() {
        Currency currency = Mocks.currency();
        assertEquals(Mocks.SYMBOL, currency.symbol());
        assertEquals(Mocks.PRICE, currency.price());
    }

    @Test
    void currencyCreationInvalidSymbolTest() {
        InvalidSymbolException exception = assertThrows(InvalidSymbolException.class, Mocks::currencyWrongSymbol);
        assertEquals("Symbol cannot be empty", exception.getMessage());
    }

    @Test
    void currencyCreationInvalidPriceTest() {
        InvalidPriceException exception = assertThrows(InvalidPriceException.class, Mocks::currencyWrongPrice);
        assertEquals("Price must be informed", exception.getMessage());
    }

    private static class Mocks {
        private Mocks() {
        }
        static final String SYMBOL = "BTC";
        static final BigDecimal PRICE = BigDecimal.valueOf(12345.67);

        static Currency currency() {
            return Currency.builder()
                    .symbol(SYMBOL)
                    .price(PRICE)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        static void currencyWrongSymbol() {
            Currency.builder()
                    .price(PRICE)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        static void currencyWrongPrice() {
            Currency.builder()
                    .symbol(SYMBOL)
                    .build();
        }
    }
}