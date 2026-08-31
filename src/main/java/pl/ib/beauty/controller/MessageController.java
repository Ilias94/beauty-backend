package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dto.ConversationDtoResponse;
import pl.ib.beauty.model.dto.MessageDtoRequest;
import pl.ib.beauty.model.dto.MessageDtoResponse;
import pl.ib.beauty.service.MessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public List<ConversationDtoResponse> getConversations() {
        return messageService.getConversations();
    }

    @GetMapping("/{partnerId}")
    public List<MessageDtoResponse> getMessages(@PathVariable Long partnerId) {
        return messageService.getMessages(partnerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDtoResponse send(@RequestBody @Valid MessageDtoRequest request) {
        return messageService.send(request);
    }

    @PatchMapping("/{partnerId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable Long partnerId) {
        messageService.markAsRead(partnerId);
    }
}
