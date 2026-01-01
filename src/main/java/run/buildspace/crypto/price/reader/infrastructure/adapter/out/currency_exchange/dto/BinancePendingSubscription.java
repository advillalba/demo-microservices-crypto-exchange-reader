package run.buildspace.crypto.price.reader.infrastructure.adapter.out.currency_exchange.dto;

import java.util.function.Consumer;

public record BinancePendingSubscription(String symbol, Consumer<Boolean> callback) {
}
