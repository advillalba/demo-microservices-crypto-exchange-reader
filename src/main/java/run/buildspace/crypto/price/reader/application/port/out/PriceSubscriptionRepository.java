package run.buildspace.crypto.price.reader.application.port.out;

import run.buildspace.crypto.price.reader.domain.model.Subscription;

import java.util.List;

public interface PriceSubscriptionRepository {
    boolean exists(String symbol);
    void save(Subscription subscription);
    void delete(String symbol);
    List<String> findAll();
    long count();
}
