package run.buildspace.crypto.price.reader.infrastructure.adapter.out.persistance;

import run.buildspace.crypto.price.reader.domain.model.Subscription;
import run.buildspace.crypto.price.reader.infrastructure.adapter.out.persistance.entity.SubscriptionEntity;
import run.buildspace.crypto.price.reader.infrastructure.adapter.out.persistance.repository.JpaPriceSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SubscriptionRepositoryAdapterTest {


    @InjectMocks
    private SubscriptionRepositoryAdapter subscriptionRepositoryAdapter;

    @Mock
    private JpaPriceSubscriptionRepository jpaPriceSubscriptionRepository;


    @Test
    void exists() {
        // given
        given(jpaPriceSubscriptionRepository.existsById(Mocks.SYMBOL)).willReturn(true);

        //when && then
        assertTrue(subscriptionRepositoryAdapter.exists(Mocks.SYMBOL));
    }

    @Test
    void save() {
        //given
        ArgumentCaptor<SubscriptionEntity> argumentCaptor = ArgumentCaptor.forClass(SubscriptionEntity.class);
        given(jpaPriceSubscriptionRepository.save(argumentCaptor.capture())).willReturn(new SubscriptionEntity());
        //when
        subscriptionRepositoryAdapter.save(Mocks.subscription());
        //then
        assertEquals(Mocks.SYMBOL, argumentCaptor.getValue().getSymbol());
    }

    @Test
    void delete() {
        //when
        subscriptionRepositoryAdapter.delete(Mocks.SYMBOL);
        //then
        then(jpaPriceSubscriptionRepository).should().deleteById(Mocks.SYMBOL);
    }

    @Test
    void countTest() {
        // given
        given(jpaPriceSubscriptionRepository.count()).willReturn(5L);

        // when
        long count = subscriptionRepositoryAdapter.count();

        // then
        assertEquals(5L, count);

    }


    private static class Mocks {
        private Mocks() {
        }

        static final String SYMBOL = "BTC";

        private static Subscription subscription() {
            return Subscription.builder().symbol(SYMBOL).subscribe(true).build();
        }

    }
}