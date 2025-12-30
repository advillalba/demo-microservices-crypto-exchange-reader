package run.buildspace.cryptoreader.infrastructure.adapter.out.persistance;

import run.buildspace.cryptoreader.application.port.out.PriceSubscriptionRepository;
import run.buildspace.cryptoreader.domain.model.Subscription;
import run.buildspace.cryptoreader.infrastructure.adapter.out.persistance.entity.SubscriptionEntity;
import run.buildspace.cryptoreader.infrastructure.adapter.out.persistance.repository.JpaPriceSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class SubscriptionRepositoryAdapter implements PriceSubscriptionRepository {

    private final JpaPriceSubscriptionRepository jpaPriceSubscriptionRepository;

    @Autowired
    public SubscriptionRepositoryAdapter(JpaPriceSubscriptionRepository jpaPriceSubscriptionRepository) {
        this.jpaPriceSubscriptionRepository = jpaPriceSubscriptionRepository;
    }
    @Override
    public boolean exists(String symbol) {
        return jpaPriceSubscriptionRepository.existsById(symbol);
    }

    @Override
    public void save(Subscription subscription) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSymbol(subscription.symbol());
        jpaPriceSubscriptionRepository.save(entity);
    }

    @Override
    public void delete(String symbol) {
        jpaPriceSubscriptionRepository.deleteById(symbol);
    }

    @Override
    public List<String> findAll() {
        return StreamSupport.stream(jpaPriceSubscriptionRepository.findAll().spliterator(), true).map(SubscriptionEntity::getSymbol).toList();

    }

    @Override
    public long count() {
        return jpaPriceSubscriptionRepository.count();
    }
}
