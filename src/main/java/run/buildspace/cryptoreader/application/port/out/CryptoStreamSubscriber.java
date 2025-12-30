package run.buildspace.cryptoreader.application.port.out;

import java.util.function.Consumer;

public interface CryptoStreamSubscriber {
    void subscribe(String symbol, Consumer<Boolean> onComplete);

    void unsubscribe(String symbol, Consumer<Boolean> onComplete);
}
