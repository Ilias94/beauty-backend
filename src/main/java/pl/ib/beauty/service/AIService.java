package pl.ib.beauty.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Faq;
import pl.ib.beauty.model.dto.AiTicketResponseDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public String query(String prompt) {
        try {
            ChatResponse chatResponse = chatModel.call(new Prompt("Wygeneruj mi opis uslugi dla tytulu: %s".formatted(prompt)));
            return chatResponse.getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("AI description generation unavailable: {}", e.getMessage());
            return "";
        }
    }

    public AiTicketResponseDto aiTicketResponse(String question, List<Faq> faqs) {

        String faqContext = faqs.stream()
                .map(faq -> "Pytanie: " + faq.getQuestion() + "\nOdpowiedź: " + faq.getAnswer())
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                
                Odpowiadaj po polsku, zwięźle i uprzejmie.
                Odpowiedz ma być w formacie json z dwoma polami answer i score.
                Score to Twoja pewność odpowiedzi od 0 do 100.
                
                Context:
                %s
                
                Question: %s
                """.formatted(faqContext, question);

        try {
            ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
            String[] split = chatResponse.getResult().getOutput().getText().split("\\R");
            String join = String.join("\n", Arrays.copyOfRange(split, 1, split.length - 1));
            return objectMapper.readValue(join, AiTicketResponseDto.class);
        } catch (Exception e) {
            log.warn("AI ticket response unavailable: {}", e.getMessage());
            return new AiTicketResponseDto(0, "");
        }
    }
}
