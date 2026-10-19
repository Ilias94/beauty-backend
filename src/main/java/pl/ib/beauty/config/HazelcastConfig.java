package pl.ib.beauty.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
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
        if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
            configureKubernetesDiscovery(config.getNetworkConfig().getJoin());
        }
        return config;
    }

    // Auto-detected Kubernetes discovery tried every pod in the namespace (Postgres included)
    // on its first container port, so members never found each other and startup waited out
    // the join timeout. Restrict it to the beauty pods and the Hazelcast port.
    private void configureKubernetesDiscovery(JoinConfig join) {
        join.getAutoDetectionConfig().setEnabled(false);
        join.getMulticastConfig().setEnabled(false);
        join.getKubernetesConfig().setEnabled(true)
                .setProperty("pod-label-name", "app")
                .setProperty("pod-label-value", "beauty")
                .setProperty("service-port", "5701");
    }
}