package run.buildspace.crypto.price.reader.infrastructure.adapter.out.messaging;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.buildspace.crypto.price.reader.application.port.out.PriceEventPublisher;
import run.buildspace.crypto.price.reader.domain.exception.MessagePublishException;
import run.buildspace.crypto.price.reader.domain.model.PriceUpdate;
import run.buildspace.crypto.price.reader.infrastructure.config.RabbitMQProperties;

@Component
public class RabbitMQPublisher implements PriceEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    @Autowired
    public RabbitMQPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(PriceUpdate priceUpdate) {
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.currencyUpdateRoutingBind().replace("#", priceUpdate.symbol()), priceUpdate);
        } catch (AmqpException e) {
            throw new MessagePublishException("Failed to publish price: " + priceUpdate.symbol(), e);
        }
    }

    @Override
    public void publishError(String message) {
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.currencyErrorUpdateRoutingBind().replace("#", "price"), message);
        } catch (AmqpException e) {
            throw new MessagePublishException("Failed to publish error message: " + message, e);
        }
    }
}
