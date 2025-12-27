package run.buildspace.cryptoreader.domain.model;

import run.buildspace.cryptoreader.domain.exception.InvalidSymbolException;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Builder
public record Subscription(boolean subscribe, String symbol) implements Serializable {
    public Subscription {
        if (StringUtils.isBlank(symbol)) {
            throw new InvalidSymbolException("Symbol cannot be empty");
        }
    }
}
