package pl.ib.beauty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.CourseStatus;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Message;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ContactDtoResponse;
import pl.ib.beauty.model.dto.ContactDtoResponse.ContactCourse;
import pl.ib.beauty.model.dto.MessageDtoRequest;
import pl.ib.beauty.model.dto.MessageDtoResponse;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.MessageRepository;
import pl.ib.beauty.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final UserService userService = mock(UserService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final DailyUsageLimiter limiter = mock(DailyUsageLimiter.class);

    private final MessageService service = new MessageService(messageRepository, userRepository, courseRepository,
            userService, notificationService, limiter);

    private final User student = user(1L, "Anna", "anna@test.pl");
    private final User instructor = user(2L, "Marta", "marta@test.pl");
    private final User otherStudent = user(3L, "Ola", "ola@test.pl");

    @BeforeEach
    void setUp() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(instructor));
        when(userRepository.findById(3L)).thenReturn(Optional.of(otherStudent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(limiter.tryAcquire(anyString())).thenReturn(5);
    }

    @Test
    void anyoneCanAskTheInstructorOfAPublishedCourse() {
        signedInAs(student);
        when(courseRepository.existsByCreatorIdAndStatus(2L, CourseStatus.PUBLISHED)).thenReturn(true);

        MessageDtoResponse sent = service.send(new MessageDtoRequest(2L, "Czy kurs jest dla początkujących?", null));

        assertThat(sent.recipientId()).isEqualTo(2L);
        verify(limiter).tryAcquire("anna@test.pl");
    }

    @Test
    void participantsCannotStartPrivateConversationsWithEachOther() {
        signedInAs(student);

        assertStatus(() -> service.send(new MessageDtoRequest(3L, "Hej", null)), HttpStatus.FORBIDDEN);
        verify(messageRepository, never()).save(any());
        verify(limiter, never()).tryAcquire(anyString());
    }

    @Test
    void instructorCanWriteToTheirOwnParticipant() {
        signedInAs(instructor);
        when(courseRepository.teachesParticipant(2L, 1L)).thenReturn(true);

        service.send(new MessageDtoRequest(1L, "Zmiana sali na jutro", null));

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void repliesInAnExistingConversationSkipTheRulesAndTheLimit() {
        signedInAs(instructor);
        when(messageRepository.existsConversation(2L, 3L)).thenReturn(true);

        service.send(new MessageDtoRequest(3L, "Dzięki za pytanie!", null));

        verify(messageRepository).save(any(Message.class));
        verify(limiter, never()).tryAcquire(anyString());
    }

    @Test
    void newConversationsOverTheDailyLimitGet429() {
        signedInAs(student);
        when(courseRepository.existsByCreatorIdAndStatus(2L, CourseStatus.PUBLISHED)).thenReturn(true);
        when(limiter.tryAcquire("anna@test.pl")).thenReturn(-1);

        assertStatus(() -> service.send(new MessageDtoRequest(2L, "Pytanie", null)), HttpStatus.TOO_MANY_REQUESTS);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void messagingYourselfIsRejected() {
        signedInAs(student);

        assertStatus(() -> service.send(new MessageDtoRequest(1L, "Notatka", null)), HttpStatus.BAD_REQUEST);
    }

    @Test
    void theCourseAMessageIsAboutMustBeTaughtByOneOfThem() {
        signedInAs(student);
        when(courseRepository.findById(9L)).thenReturn(Optional.of(course(9L, "Masaż", otherStudent)));

        assertStatus(() -> service.send(new MessageDtoRequest(2L, "Pytanie", 9L)), HttpStatus.BAD_REQUEST);
    }

    @Test
    void aQuestionFromACoursePageCarriesThatCourse() {
        signedInAs(student);
        when(courseRepository.existsByCreatorIdAndStatus(2L, CourseStatus.PUBLISHED)).thenReturn(true);
        when(courseRepository.findById(16L)).thenReturn(Optional.of(course(16L, "Stylizacja rzęs", instructor)));

        MessageDtoResponse sent = service.send(new MessageDtoRequest(2L, "Czy są wolne miejsca?", 16L));

        assertThat(sent.courseId()).isEqualTo(16L);
        assertThat(sent.courseTitle()).isEqualTo("Stylizacja rzęs");
    }

    @Test
    void contactsAreInstructorsOfBookedCoursesAndParticipantsOfTaughtOnesMergedPerPerson() {
        signedInAs(instructor);
        Course lashes = course(16L, "Stylizacja rzęs", instructor, student, otherStudent);
        Course nails = course(17L, "Manicure", instructor, student);
        Course booked = course(20L, "Masaż", user(4L, "Ewa", "ewa@test.pl"), instructor);
        when(courseRepository.findBookedWithCreator(2L)).thenReturn(List.of(booked));
        when(courseRepository.findTaughtWithParticipants(2L)).thenReturn(List.of(lashes, nails));

        List<ContactDtoResponse> contacts = service.getContacts();

        assertThat(contacts).extracting(ContactDtoResponse::firstName).containsExactly("Anna", "Ewa", "Ola");
        assertThat(contacts.get(0).courses()).containsExactly(
                new ContactCourse(16L, "Stylizacja rzęs", "PARTICIPANT"),
                new ContactCourse(17L, "Manicure", "PARTICIPANT"));
        assertThat(contacts.get(1).courses()).containsExactly(new ContactCourse(20L, "Masaż", "INSTRUCTOR"));
    }

    private void signedInAs(User user) {
        when(userService.currentLoginUser()).thenReturn(user);
    }

    private static void assertStatus(Runnable call, HttpStatus status) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    private static User user(Long id, String firstName, String email) {
        return User.builder().id(id).firstName(firstName).lastName("Test").email(email).build();
    }

    private static Course course(Long id, String title, User creator, User... participants) {
        return Course.builder().id(id).title(title).creator(creator).participants(new ArrayList<>(List.of(participants))).build();
    }
}
