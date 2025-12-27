package run.buildspace.cryptoreader.infrastructure.adapter.common;

import run.buildspace.cryptoreader.infrastructure.config.RabbitMQProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQManager {

    private final RabbitMQProperties properties;

    @Autowired
    public RabbitMQManager(RabbitMQProperties properties) {
        this.properties = properties;
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(properties.exchange());
    }

    @Bean
    Queue queueCurrencyUpdate() {
        return new Queue(properties.currencyUpdateQueue(), true);
    }

    @Bean
    Binding bindingCurrencyUpdate(Queue queueCurrencyUpdate, TopicExchange exchange) {
        return BindingBuilder.bind(queueCurrencyUpdate).to(exchange).with(properties.currencyUpdateRoutingBind());
    }


    @Bean
    Queue queueSubscription() {
        return new Queue(properties.subscriptionQueue(), true);
    }

    @Bean
    Binding bindingSubscription(Queue queueSubscription, TopicExchange exchange) {
        return BindingBuilder.bind(queueSubscription).to(exchange).with(properties.subscriptionRoutingBind());
    }

    @Bean
    Binding bindingUnsubscription(Queue queueSubscription, TopicExchange exchange) {
        return BindingBuilder.bind(queueSubscription).to(exchange).with(properties.unsubscribeRoutingBind());
    }

    @Bean
    Queue queueErrorCurrencyUpdate() {
        return QueueBuilder.durable(properties.currencyErrorUpdateQueue()).deadLetterExchange(properties.deadLetterExchange()).build();

    }

    @Bean
    Binding bindingErrorCurrencyUpdate(Queue queueErrorCurrencyUpdate, TopicExchange exchange) {
        return BindingBuilder.bind(queueErrorCurrencyUpdate).to(exchange).with(properties.currencyErrorUpdateRoutingBind());
    }

    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(properties.deadLetterExchange());
    }

    @Bean
    Queue deadLetterQueue() {
        return new Queue(properties.deadLetterQueue(), true);
    }


    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
