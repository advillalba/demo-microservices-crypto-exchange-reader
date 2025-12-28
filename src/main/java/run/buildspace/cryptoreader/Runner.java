package run.buildspace.cryptoreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import run.buildspace.cryptoreader.application.service.SubscriptionService;

@Component
public class Runner implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(Runner.class);

    private final SubscriptionService subscriptionService;

    @Autowired
    public Runner(SubscriptionService subscriptionService) {

        this.subscriptionService = subscriptionService;

    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting application...");
        subscriptionService.reloadAllSubscriptions();

    }

}
