package pl.ib.beauty.config;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.ib.beauty.service.DailyUsageLimiter;

import java.time.Clock;

@Configuration
public class AssistantConfig {

    public static final String USAGE_MAP = "assistant-usage";

    @Bean
    public DailyUsageLimiter assistantUsageLimiter(HazelcastInstance hazelcastInstance,
                                                       @Value("${assistant.dailyLimit}") int dailyLimit) {
        return new DailyUsageLimiter(hazelcastInstance.getMap(USAGE_MAP), dailyLimit, Clock.systemDefaultZone());
    }
}
