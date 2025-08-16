package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatModel chatModel;

    public String query(String prompt) {
        ChatResponse chatResponse = chatModel.call(new Prompt("Wygeneruj mi opis uslugi dla tytulu: %s".formatted(prompt)));
        return chatResponse.getResult().getOutput().getText();
    }
}
