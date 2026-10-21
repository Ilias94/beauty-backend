package pl.ib.beauty;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import pl.ib.beauty.config.HazelcastConfig;

import java.util.List;

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
        "stripe.secretKey=test-key",
        "spring.security.oauth2.client.registration.google.client-id=test-client",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret",
        // The test profile points mail at Gmail; don't let its health check depend on the network.
        "management.health.mail.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureRestTestClient
@Import(ApplicationSmokeTest.IsolatedHazelcast.class)
class ApplicationSmokeTest extends PostgreSqlTestContainer {

    // The real model is a 470 MB download; startup only needs a bean of this type.
    @MockitoBean
    EmbeddingModel embeddingModel;

    @Autowired
    RestTestClient client;

    @Test
    void startsAndServesHealthCatalogAndApiDocs() {
        assertThat(getOk("/actuator/health")).contains("\"status\":\"UP\"");
        getOk("/api/v1/courses?page=0&size=1");
        // The frontend's API client is generated from this document.
        assertThat(getOk("/v3/api-docs")).contains("\"operationId\":\"answerTicketQuestion\"");
    }

    @Test
    void keepsSecretsAndPaidMaterialPrivate() {
        // The Kubernetes probes and the Jenkins smoke test read this one.
        assertThat(getOk("/actuator/health/readiness")).contains("\"status\":\"UP\"");
        // These would hand out the environment's secrets (keys, passwords) to anyone.
        for (String endpoint : List.of("/actuator/env", "/actuator/heapdump", "/actuator/configprops", "/actuator/beans")) {
            client.get().uri(endpoint).exchange().expectStatus().is4xxClientError();
        }
        // Without signing in: no signed links to a course's recordings, no changes to them.
        client.get().uri("/api/v1/video/1").exchange().expectStatus().is4xxClientError();
        client.delete().uri("/api/v1/video/1").exchange().expectStatus().is4xxClientError();
    }

    @Test
    void letsOnlyTheConfiguredFrontendCallTheApiFromABrowser() {
        // cors.url in the test profile is the local frontend.
        client.options().uri("/api/v1/courses")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200");
        client.options().uri("/api/v1/courses")
                .header(HttpHeaders.ORIGIN, "https://beauty-front.onrender.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus().isForbidden();
    }

    private String getOk(String uri) {
        return client.get().uri(uri).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
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
