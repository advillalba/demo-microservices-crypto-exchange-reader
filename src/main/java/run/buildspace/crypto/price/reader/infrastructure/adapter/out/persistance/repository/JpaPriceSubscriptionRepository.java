package run.buildspace.crypto.price.reader.infrastructure.adapter.out.persistance.repository;

import run.buildspace.crypto.price.reader.infrastructure.adapter.out.persistance.entity.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;

public interface JpaPriceSubscriptionRepository extends CrudRepository<SubscriptionEntity, String> {
}
