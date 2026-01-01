package run.buildspace.crypto.price.reader.infrastructure.adapter.in.messaging;

import com.rabbitmq.client.Channel;
import run.buildspace.crypto.price.reader.application.port.in.ForSubscriptionUpdate;
import run.buildspace.crypto.price.reader.domain.model.PendingSubscription;
import run.buildspace.crypto.price.reader.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Mock
    private ForSubscriptionUpdate forSubscriptionUpdate;

    @Mock
    private Channel channel;

    private ArgumentCaptor<PendingSubscription> pendingSubscriptionCaptor;

    @BeforeEach
    void setUp() {
        pendingSubscriptionCaptor = ArgumentCaptor.forClass(PendingSubscription.class);
    }

    @Test
    void receiveMessageSendACKTest() throws IOException {
        //given
        willDoNothing().given(forSubscriptionUpdate).handleSubscription(pendingSubscriptionCaptor.capture());

        //when
       Subscription binanceSubscription = Mocks.subscription();
       rabbitMQConsumer.receiveMessage(binanceSubscription, channel, Mocks.DELIVERY_TAG);
       pendingSubscriptionCaptor.getValue().onSuccess().run();

       //then
        then(channel).should().basicAck(Mocks.DELIVERY_TAG, false);

    }

    @Test
    void receiveMessageSendErrorACKTest() throws IOException {
        //given
        willDoNothing().given(forSubscriptionUpdate).handleSubscription(pendingSubscriptionCaptor.capture());
        willThrow(new IOException()).given(channel).basicAck(Mocks.DELIVERY_TAG, false);
        //when
        Subscription binanceSubscription = Mocks.subscription();
        rabbitMQConsumer.receiveMessage(binanceSubscription, channel, Mocks.DELIVERY_TAG);
        pendingSubscriptionCaptor.getValue().onSuccess().run();

        //then
        then(channel).should().basicAck(Mocks.DELIVERY_TAG, false);

    }


    @Test
    void receiveMessageSendNACKTest() throws IOException {
        //given
        willDoNothing().given(forSubscriptionUpdate).handleSubscription(pendingSubscriptionCaptor.capture());

        //when
        Subscription binanceSubscription = Mocks.subscription();
        rabbitMQConsumer.receiveMessage(binanceSubscription, channel, Mocks.DELIVERY_TAG);
        pendingSubscriptionCaptor.getValue().onFailure().run();

        //then
        then(channel).should().basicNack(Mocks.DELIVERY_TAG, false, true);

    }

    @Test
    void receiveMessageSendErrorNACKTest() throws IOException {
        //given
        willDoNothing().given(forSubscriptionUpdate).handleSubscription(pendingSubscriptionCaptor.capture());
        willThrow(new IOException()).given(channel).basicNack(Mocks.DELIVERY_TAG, false, true);
        //when
        Subscription binanceSubscription = Mocks.subscription();
        rabbitMQConsumer.receiveMessage(binanceSubscription, channel, Mocks.DELIVERY_TAG);
        pendingSubscriptionCaptor.getValue().onFailure().run();

        //then
        then(channel).should().basicNack(Mocks.DELIVERY_TAG, false, true);

    }

    private static class Mocks{
        private Mocks() {
        }

        private static final long DELIVERY_TAG = 1L;

        private static Subscription subscription(){
            return new PodamFactoryImpl().manufacturePojo(Subscription.class);
        }
    }
}