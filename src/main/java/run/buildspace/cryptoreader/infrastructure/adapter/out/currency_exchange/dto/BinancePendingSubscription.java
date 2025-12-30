package run.buildspace.cryptoreader.infrastructure.adapter.out.currency_exchange.dto;

import java.util.function.Consumer;

public record BinancePendingSubscription(String symbol, Consumer<Boolean> callback) {
}
