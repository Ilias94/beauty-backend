package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.CourseStatus;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Message;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ContactDtoResponse;
import pl.ib.beauty.model.dto.ContactDtoResponse.ContactCourse;
import pl.ib.beauty.model.dto.ConversationDtoResponse;
import pl.ib.beauty.model.dto.MessageDtoRequest;
import pl.ib.beauty.model.dto.MessageDtoResponse;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.MessageRepository;
import pl.ib.beauty.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Private messages. Who may start a conversation:
 * <ul>
 *   <li>anyone signed in, with the instructor of a published course (questions before booking);</li>
 *   <li>an instructor, with the participants of their own courses.</li>
 * </ul>
 * Once a conversation exists both sides can reply. Participants don't message each other
 * privately (the course group chat is for that), and new conversations are capped per day.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final DailyUsageLimiter newConversationLimiter;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository,
                          CourseRepository courseRepository, UserService userService,
                          NotificationService notificationService,
                          @Qualifier("newConversationLimiter") DailyUsageLimiter newConversationLimiter) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.newConversationLimiter = newConversationLimiter;
    }

    @Transactional
    public MessageDtoResponse send(MessageDtoRequest request) {
        User sender = userService.currentLoginUser();
        if (sender.getId().equals(request.recipientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot message yourself");
        }
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(EntityNotFoundException::new);
        Course course = courseContext(request.courseId(), sender, recipient);

        if (!messageRepository.existsConversation(sender.getId(), recipient.getId())) {
            if (!canStartConversation(sender, recipient)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't start a conversation with this user");
            }
            if (newConversationLimiter.tryAcquire(sender.getEmail()) < 0) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily limit of new conversations reached");
            }
        }

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .course(course)
                .content(request.content())
                .build();

        Message saved = messageRepository.save(message);

        MessageDtoResponse response = toDto(saved);
        notificationService.sendChatMessage(recipient.getEmail(), response);
        return response;
    }

    /** People the current user may start a conversation with from the contact list, with the courses that connect them. */
    @Transactional(readOnly = true)
    public List<ContactDtoResponse> getContacts() {
        User current = userService.currentLoginUser();
        Map<Long, ContactBuilder> contacts = new LinkedHashMap<>();

        for (Course course : courseRepository.findBookedWithCreator(current.getId())) {
            add(contacts, current, course.getCreator(), course, "INSTRUCTOR");
        }
        for (Course course : courseRepository.findTaughtWithParticipants(current.getId())) {
            for (User participant : course.getParticipants()) {
                add(contacts, current, participant, course, "PARTICIPANT");
            }
        }

        return contacts.values().stream()
                .map(ContactBuilder::build)
                .sorted(Comparator.comparing(ContactDtoResponse::firstName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ContactDtoResponse::lastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
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

    private boolean canStartConversation(User sender, User recipient) {
        return courseRepository.existsByCreatorIdAndStatus(recipient.getId(), CourseStatus.PUBLISHED)
                || courseRepository.teachesParticipant(sender.getId(), recipient.getId());
    }

    /** The course a message is about must be one that one of the two people teaches. */
    private Course courseContext(Long courseId, User sender, User recipient) {
        if (courseId == null) {
            return null;
        }
        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        Long creatorId = course.getCreator().getId();
        if (!creatorId.equals(sender.getId()) && !creatorId.equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The course is not taught by either person");
        }
        return course;
    }

    private static void add(Map<Long, ContactBuilder> contacts, User current, User person, Course course, String role) {
        if (person == null || Objects.equals(person.getId(), current.getId())) {
            return;
        }
        contacts.computeIfAbsent(person.getId(), id -> new ContactBuilder(person))
                .courses.add(new ContactCourse(course.getId(), course.getTitle(), role));
    }

    private static final class ContactBuilder {
        private final User user;
        private final List<ContactCourse> courses = new ArrayList<>();

        private ContactBuilder(User user) {
            this.user = user;
        }

        private ContactDtoResponse build() {
            return new ContactDtoResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getFileName(), courses);
        }
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
        Course course = m.getCourse();
        return new MessageDtoResponse(
                m.getId(),
                m.getSender().getId(),
                m.getRecipient().getId(),
                m.getContent(),
                m.getSentAt(),
                m.isRead(),
                course != null ? course.getId() : null,
                course != null ? course.getTitle() : null
        );
    }
}
