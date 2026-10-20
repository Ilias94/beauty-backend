package pl.ib.beauty;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.ib.beauty.config.HazelcastConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the whole application against a real Postgres (Flyway migrations, JPA, security,
 * Hazelcast, Spring AI wiring, springdoc) and checks it serves requests. The other tests are unit
 * tests, so this is what catches a dependency or JDK upgrade that breaks startup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        // Placeholders for secrets: nothing in this test calls the real services.
        "spring.ai.anthropic.api-key=test-key",
        "google.accessKey=test-key",
        "aws.accessKey=test-key",
        "aws.secretKey=test-secret",
        "stripe.secretKey=sk_test_placeholder",
        "spring.security.oauth2.client.registration.google.client-id=test-client",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret",
        // The test profile points mail at Gmail; don't let its health check depend on the network.
        "management.health.mail.enabled=false"
})
@ActiveProfiles("test")
@Import(ApplicationSmokeTest.IsolatedHazelcast.class)
class ApplicationSmokeTest extends PostgreSqlTestContainer {

    // The real model is a 470 MB download; startup only needs a bean of this type.
    @MockitoBean
    EmbeddingModel embeddingModel;

    @Autowired
    TestRestTemplate rest;

    @Test
    void startsAndServesHealthCatalogAndApiDocs() {
        ResponseEntity<String> health = rest.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).contains("\"status\":\"UP\"");

        ResponseEntity<String> courses = rest.getForEntity("/api/v1/courses?page=0&size=1", String.class);
        assertThat(courses.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The frontend's API client is generated from this document.
        ResponseEntity<String> apiDocs = rest.getForEntity("/v3/api-docs", String.class);
        assertThat(apiDocs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apiDocs.getBody()).contains("\"operationId\":\"answerTicketQuestion\"");
    }

    /** Its own cluster with discovery off, so the test never joins a locally running backend's cache. */
    @TestConfiguration
    static class IsolatedHazelcast {
        @Bean
        @Primary
        Config isolatedHazelcastConfig() {
            Config config = new HazelcastConfig().configHazelcast();
            config.setInstanceName("hazelcast-smoke-test");
            config.setClusterName("beauty-smoke-test");
            JoinConfig join = config.getNetworkConfig().getJoin();
            join.getAutoDetectionConfig().setEnabled(false);
            join.getMulticastConfig().setEnabled(false);
            join.getTcpIpConfig().setEnabled(false);
            return config;
        }
    }
}
