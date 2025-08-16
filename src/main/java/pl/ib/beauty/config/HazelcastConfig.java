package pl.ib.beauty.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.ib.beauty.model.dao.Course;

@Configuration
public class HazelcastConfig {
    @Bean
    public Config configHazelcast() {
        Config config = new Config();
        config.setInstanceName("hazelcast-instance");
        MapConfig mapConfig = new MapConfig("course");
        mapConfig.setTimeToLiveSeconds(3600);
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        mapConfig.setEvictionConfig(evictionConfig);
        config.getSerializationConfig().addDataSerializableFactory(1, new MyDataSerializableFactory());
        return config;
    }
}