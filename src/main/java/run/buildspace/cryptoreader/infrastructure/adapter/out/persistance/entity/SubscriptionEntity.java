package run.buildspace.cryptoreader.infrastructure.adapter.out.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    private String symbol;


}
