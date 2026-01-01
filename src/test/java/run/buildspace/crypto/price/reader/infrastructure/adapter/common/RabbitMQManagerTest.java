package run.buildspace.crypto.price.reader.infrastructure.adapter.common;


import run.buildspace.crypto.price.reader.infrastructure.config.RabbitMQProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class RabbitMQManagerTest {

    private RabbitMQManager rabbitMQManager;

    private RabbitMQProperties rabbitMQProperties;

    @BeforeEach
    void setUp() {
        rabbitMQProperties = Mocks.rabbitMQProperties();
        rabbitMQManager = new RabbitMQManager(rabbitMQProperties);
    }

    @Test
    void exchangeTest() {
        TopicExchange exchange = rabbitMQManager.exchange();
        assertEquals(rabbitMQProperties.exchange(), exchange.getName());
    }

    @Test
    void queueCurrencyUpdateTest() {
        Queue queue = rabbitMQManager.queueCurrencyUpdate();
        assertEquals(rabbitMQProperties.currencyUpdateQueue(), queue.getName());
    }

    @Test
    void bindingCurrencyUpdateTest() {
        Binding binding = rabbitMQManager.bindingCurrencyUpdate(rabbitMQManager.queueCurrencyUpdate(), rabbitMQManager.exchange());
        assertEquals(rabbitMQManager.exchange().getName(), binding.getExchange());
        assertEquals(rabbitMQManager.queueCurrencyUpdate().getName(), binding.getDestination());
        assertEquals(rabbitMQProperties.currencyUpdateRoutingBind(), binding.getRoutingKey());
    }

    @Test
    void queueSubscriptionTest() {
        Queue queue = rabbitMQManager.queueSubscription();
        assertEquals(rabbitMQProperties.subscriptionQueue(), queue.getName());
    }

    @Test
    void bindingSubscriptionTest() {
        Binding binding = rabbitMQManager.bindingSubscription(rabbitMQManager.queueSubscription(), rabbitMQManager.exchange());
        assertEquals(rabbitMQManager.exchange().getName(), binding.getExchange());
        assertEquals(rabbitMQManager.queueSubscription().getName(), binding.getDestination());
        assertEquals(rabbitMQProperties.subscriptionRoutingBind(), binding.getRoutingKey());
    }


    @Test
    void queueErrorCurrencyUpdateTest() {
        Queue queue = rabbitMQManager.queueErrorCurrencyUpdate();
        assertEquals(rabbitMQProperties.currencyErrorUpdateQueue(), queue.getName());
    }

    @Test
    void bindingErrorCurrencyUpdateTest() {
        Binding binding = rabbitMQManager.bindingErrorCurrencyUpdate(rabbitMQManager.queueErrorCurrencyUpdate(), rabbitMQManager.exchange());
        assertEquals(rabbitMQManager.exchange().getName(), binding.getExchange());
        assertEquals(rabbitMQManager.queueErrorCurrencyUpdate().getName(), binding.getDestination());
        assertEquals(rabbitMQProperties.currencyErrorUpdateRoutingBind(), binding.getRoutingKey());
    }

    @Test
    void deadLetterExchangeTest() {
        assertEquals(rabbitMQProperties.deadLetterExchange(), rabbitMQManager.deadLetterExchange().getName());
    }

    @Test
    void deadLetterQueueTest() {
        Queue queue = rabbitMQManager.deadLetterQueue();
        assertEquals(rabbitMQProperties.deadLetterQueue(), queue.getName());
    }

    @Test
    void deadLetterBindingTest() {
        Binding binding = rabbitMQManager.deadLetterBinding(rabbitMQManager.deadLetterQueue(), rabbitMQManager.deadLetterExchange());
        assertEquals(rabbitMQManager.deadLetterExchange().getName(), binding.getExchange());
        assertEquals(rabbitMQManager.deadLetterQueue().getName(), binding.getDestination());
    }

    @Test
    void jsonMessageConverterTest() {
        assertInstanceOf(Jackson2JsonMessageConverter.class, rabbitMQManager.jsonMessageConverter());
    }

    private static class Mocks {
        private Mocks() {
        }

        private static RabbitMQProperties rabbitMQProperties() {
            return new PodamFactoryImpl().manufacturePojo(RabbitMQProperties.class);
        }
    }
}