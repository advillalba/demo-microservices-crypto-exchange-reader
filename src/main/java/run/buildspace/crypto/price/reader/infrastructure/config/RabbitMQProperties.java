package run.buildspace.crypto.price.reader.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitMQProperties(String exchange,
                                 String currencyUpdateQueue,
                                 String currencyUpdateRoutingBind,
                                 String subscriptionQueue,
                                 String subscriptionRoutingBind,
                                 String currencyErrorUpdateQueue,
                                 String currencyErrorUpdateRoutingBind,
                                 String deadLetterExchange,
                                 String deadLetterQueue) {

}
