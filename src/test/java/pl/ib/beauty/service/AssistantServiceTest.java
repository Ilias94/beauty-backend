package pl.ib.beauty.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.dto.AssistantDtoRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssistantServiceTest {

    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
    private final AssistantUsageLimiter usageLimiter = mock(AssistantUsageLimiter.class);
    private final AssistantDtoRequest request = new AssistantDtoRequest("Czy moja płatność przeszła?", List.of());

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anna@test.pl", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void disabledAssistantDoesNotCallClaudeOrCountUsage() {
        AssistantService service = service(false);

        assertThatThrownBy(() -> service.ask(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getStatusCode())
                                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        verify(usageLimiter, never()).tryAcquire(anyString());
        verify(chatClient, never()).prompt();
    }

    @Test
    void userOverTheDailyLimitGets429WithoutCallingClaude() {
        when(usageLimiter.tryAcquire("anna@test.pl")).thenReturn(-1);
        AssistantService service = service(true);

        assertThatThrownBy(() -> service.ask(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getStatusCode())
                                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        verify(chatClient, never()).prompt();
    }

    private AssistantService service(boolean enabled) {
        return new AssistantService(chatClientBuilder, mock(AssistantTools.class), usageLimiter, enabled);
    }
}
