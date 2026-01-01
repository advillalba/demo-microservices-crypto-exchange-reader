package run.buildspace.crypto.price.reader.infrastructure.adapter.in.crypto_exchange.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class BinanceSubscription {

    private SubscriptionType method;
    @Singular
    private List<String> params;
    private long id;

}
