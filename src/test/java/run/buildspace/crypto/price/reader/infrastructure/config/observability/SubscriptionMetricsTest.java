package run.buildspace.crypto.price.reader.infrastructure.config.observability;

import run.buildspace.crypto.price.reader.application.port.out.PriceSubscriptionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class SubscriptionMetricsTest {

    @Mock
    private PriceSubscriptionRepository repository;

    @Test
    void initMetricsTest() {
        // given
        MeterRegistry registry = new SimpleMeterRegistry();
        SubscriptionMetrics subscriptionMetrics = new SubscriptionMetrics(registry, repository);

        // when
        subscriptionMetrics.initMetrics();

        // then
        Gauge gauge = registry.find("subscriptions.active").gauge();
        assertNotNull(gauge, "Gauge 'subscriptions.active' should be registered");
    }

    @Test
    void retrieveValuesTest() {
        // given
        MeterRegistry registry = new SimpleMeterRegistry();
        given(repository.count()).willReturn(5L);
        SubscriptionMetrics subscriptionMetrics = new SubscriptionMetrics(registry, repository);
        subscriptionMetrics.initMetrics();

        // when
        Gauge gauge = registry.find("subscriptions.active").gauge();
        double value = gauge.value();

        // then
        assertEquals(5.0, value, "Gauge should return repository count");
    }
}
