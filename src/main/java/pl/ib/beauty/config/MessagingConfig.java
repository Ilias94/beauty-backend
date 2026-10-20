package pl.ib.beauty.config;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.ib.beauty.service.DailyUsageLimiter;

import java.time.Clock;

@Configuration
public class MessagingConfig {

    public static final String NEW_CONVERSATIONS_MAP = "message-new-conversations";

    /** Any signed-in user may ask an instructor a question, so starting conversations is capped per day. */
    @Bean
    public DailyUsageLimiter newConversationLimiter(HazelcastInstance hazelcastInstance,
                                                    @Value("${messaging.newConversationsPerDay}") int dailyLimit) {
        return new DailyUsageLimiter(hazelcastInstance.getMap(NEW_CONVERSATIONS_MAP), dailyLimit, Clock.systemDefaultZone());
    }
}
