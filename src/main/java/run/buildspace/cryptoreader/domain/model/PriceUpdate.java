package run.buildspace.cryptoreader.domain.model;

import run.buildspace.cryptoreader.domain.exception.InvalidPriceException;
import run.buildspace.cryptoreader.domain.exception.InvalidSymbolException;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record PriceUpdate(String symbol, BigDecimal price, Long timestamp) implements Serializable {
    public PriceUpdate {
        if (StringUtils.isBlank(symbol)) {
            throw new InvalidSymbolException("Symbol cannot be empty");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        validate("Price", price);

    }

    private void validate(String value, Object field) {
        if (field == null) {
            throw new InvalidPriceException(value + " must be informed");
        }
    }

}
