package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ib.beauty.model.dao.Message;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ConversationDtoResponse;
import pl.ib.beauty.model.dto.MessageDtoRequest;
import pl.ib.beauty.model.dto.MessageDtoResponse;
import pl.ib.beauty.repository.MessageRepository;
import pl.ib.beauty.repository.UserRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public MessageDtoResponse send(MessageDtoRequest request) {
        User sender = userService.currentLoginUser();
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(EntityNotFoundException::new);

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.content())
                .build();

        Message saved = messageRepository.save(message);

        MessageDtoResponse response = toDto(saved);
        notificationService.sendChatMessage(recipient.getEmail(), response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageDtoResponse> getMessages(Long partnerId) {
        Long currentId = userService.currentLoginUser().getId();
        return messageRepository.findConversation(currentId, partnerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationDtoResponse> getConversations() {
        User current = userService.currentLoginUser();
        Long currentId = current.getId();

        Set<User> partners = new HashSet<>();
        partners.addAll(messageRepository.findRecipients(currentId));
        partners.addAll(messageRepository.findSenders(currentId));

        return partners.stream()
                .map(partner -> buildConversation(currentId, partner))
                .sorted(Comparator.comparing(ConversationDtoResponse::lastMessageAt).reversed())
                .toList();
    }

    @Transactional
    public void markAsRead(Long partnerId) {
        Long currentId = userService.currentLoginUser().getId();
        messageRepository.markConversationAsRead(partnerId, currentId);
    }

    private ConversationDtoResponse buildConversation(Long currentId, User partner) {
        Message last = messageRepository.findLastMessage(currentId, partner.getId()).orElseThrow();
        long unread = messageRepository.countBySenderIdAndRecipientIdAndReadFalse(partner.getId(), currentId);
        return new ConversationDtoResponse(
                partner.getId(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getFileName(),
                last.getContent(),
                last.getSentAt(),
                unread
        );
    }

    private MessageDtoResponse toDto(Message m) {
        return new MessageDtoResponse(
                m.getId(),
                m.getSender().getId(),
                m.getRecipient().getId(),
                m.getContent(),
                m.getSentAt(),
                m.isRead()
        );
    }
}
