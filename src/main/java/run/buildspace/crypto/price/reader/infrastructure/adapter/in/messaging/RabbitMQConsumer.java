package run.buildspace.crypto.price.reader.infrastructure.adapter.in.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import run.buildspace.crypto.price.reader.application.port.in.ForSubscriptionUpdate;
import run.buildspace.crypto.price.reader.domain.model.PendingSubscription;
import run.buildspace.crypto.price.reader.domain.model.Subscription;
import run.buildspace.crypto.price.reader.infrastructure.config.observability.Trace;

import java.io.IOException;

@Component
public class RabbitMQConsumer {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final ForSubscriptionUpdate forSubscriptionUpdate;

    @Autowired
    public RabbitMQConsumer(ForSubscriptionUpdate forSubscriptionUpdate) {
        this.forSubscriptionUpdate = forSubscriptionUpdate;
    }

    @RabbitListener(queues = "${rabbitmq.subscription-queue}")
    public void receiveMessage(Subscription subscription, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Trace.trace(this::handleSubscription, subscription, channel, deliveryTag);
    }

    private void handleSubscription(Subscription subscription, Channel channel, long deliveryTag) {
        PendingSubscription pendingSubscription = new PendingSubscription(subscription,
                () -> ack(channel, deliveryTag),    // onSuccess
                () -> nack(channel, deliveryTag)    // onFailure
        );

        forSubscriptionUpdate.handleSubscription(pendingSubscription);
    }

    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            logger.error("ACK failed", e);
        }
    }

    private void nack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            logger.error("NACK failed", e);
        }
    }


}
