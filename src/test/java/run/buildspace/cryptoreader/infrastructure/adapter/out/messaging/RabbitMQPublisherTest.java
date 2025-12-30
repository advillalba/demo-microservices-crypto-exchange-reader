package run.buildspace.cryptoreader.infrastructure.adapter.out.messaging;

import run.buildspace.cryptoreader.domain.exception.MessagePublishException;
import run.buildspace.cryptoreader.domain.model.PriceUpdate;
import run.buildspace.cryptoreader.infrastructure.config.RabbitMQProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class RabbitMQPublisherTest {


    private RabbitMQPublisher rabbitMQPublisher;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQProperties properties;


    @BeforeEach
    void setUp() {
        properties = Mocks.rabbitMQProperties();
        rabbitMQPublisher = new RabbitMQPublisher(rabbitTemplate, properties);
    }

    @Test
    void publishTest() {
        // given
        PriceUpdate priceUpdate = Mocks.priceUpdate();

        // when
        rabbitMQPublisher.publish(priceUpdate);

        // then
        then(rabbitTemplate).should(times(1)).convertAndSend(properties.exchange(), properties.currencyUpdateRoutingBind().replace("#", priceUpdate.symbol()), priceUpdate);

    }

    @Test
    void publishWithErrorTest() {
        // given
        PriceUpdate priceUpdate = Mocks.priceUpdate();
        willThrow(new AmqpException("Woops!")).given(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PriceUpdate.class));

        // when
        MessagePublishException mpe = assertThrows(MessagePublishException.class, () -> rabbitMQPublisher.publish(priceUpdate));

        // then
        assertEquals("Failed to publish price: " + priceUpdate.symbol(), mpe.getMessage());

    }

    @Test
    void publishErrorTest() {
        // given
        String message = "Test message";

        // when
        rabbitMQPublisher.publishError(message);

        // then
        then(rabbitTemplate).should(times(1)).convertAndSend(properties.exchange(), properties.currencyErrorUpdateRoutingBind().replace("#", "price"), message);
    }

    @Test
    void publishErrorWithErrorTest() {
        // given
        String message = "Test message";
        willThrow(new AmqpException("Woops!")).given(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // when
        MessagePublishException mpe = assertThrows(MessagePublishException.class, () -> rabbitMQPublisher.publishError(message));

        // then
        assertEquals("Failed to publish error message: " + message, mpe.getMessage());


    }

    private static class Mocks {
        private Mocks() {
        }

        private static final String CURRENCY_SYMBOL = "BTC";
        private static final String PRICE_TEXT = "25.0";
        private static final BigDecimal PRICE = new BigDecimal(PRICE_TEXT);

        private static PriceUpdate priceUpdate() {
            return PriceUpdate.builder().price(PRICE).symbol(CURRENCY_SYMBOL).timestamp(System.currentTimeMillis()).build();
        }

        private static RabbitMQProperties rabbitMQProperties() {
            return new PodamFactoryImpl().manufacturePojo(RabbitMQProperties.class);
        }
    }
}