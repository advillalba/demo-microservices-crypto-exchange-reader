package run.buildspace.crypto.price.reader.infrastructure.config.observability;

import run.buildspace.crypto.price.reader.application.port.out.PriceSubscriptionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMetrics {

    private final MeterRegistry registry;
    private final PriceSubscriptionRepository repository;

    @Autowired
    public SubscriptionMetrics(MeterRegistry registry,PriceSubscriptionRepository repository) {
        this.registry = registry;
        this.repository = repository;
    }

    @PostConstruct
    void initMetrics() {
        Gauge.builder("subscriptions.active", repository, PriceSubscriptionRepository::count)
                .description("Active subscriptions in database")
                .register(registry);
    }
}