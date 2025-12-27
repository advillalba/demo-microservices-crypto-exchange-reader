package run.buildspace.cryptoreader;

import run.buildspace.cryptoreader.application.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class Runner implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(Runner.class);

    private final RabbitTemplate rabbitTemplate;
    private final SubscriptionService subscriptionService;

    @Autowired
    public Runner(RabbitTemplate rabbitTemplate, SubscriptionService subscriptionService) {
        this.rabbitTemplate = rabbitTemplate;
        this.subscriptionService = subscriptionService;

    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting application...");
        subscriptionService.reloadAllSubscriptions();
       //List.of("BTC", "ETH", "USDT", "BNB", "SOL", "XRP", "USDC", "ADA", "AVAX", "DOGE",
       //                "DOT", "TRX", "LINK", "MATIC", "SHIB", "WBTC", "LTC", "DAI", "BCH", "UNI",
       //                "LEO", "ATOM", "OKB", "ETC", "XMR", "KAS", "ICP", "FIL", "LUNC", "HBAR",
       //                "APT", "NEAR", "OP", "ARB", "VET", "RNDR", "INJ", "STX", "MNT", "TIA",
       //                "GRT", "MKR", "THETA", "ALGO", "SEI", "BSV", "EGLD", "FLOW", "QNT", "AAVE",
       //                "FTM", "SAND", "MANA", "AXS", "CHZ", "NEO", "EOS", "IOTA", "KCS", "CFX",
       //                "SNX", "GALA", "MINA", "KAVA", "DYDX", "CRV", "COMP", "DASH", "ZEC", "XTZ",
       //                "CAKE", "ROSE", "WOO", "FET", "AGIX", "OCEAN", "RUNE", "LDO", "PEPE", "BONK",
       //                "FLOKI", "ORDI", "SATS", "JUP", "PYTH")
       //        .forEach(symbol -> rabbitTemplate.convertAndSend("cryptocurrencies", "currency.subscription.handle", new Subscription(true, symbol)));

        new CountDownLatch(1).await();
    }

}
