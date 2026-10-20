package pl.ib.beauty.model.dto;

import java.util.List;

/** Someone the current user can start a private conversation with, and the courses that connect them. */
public record ContactDtoResponse(
        Long id,
        String firstName,
        String lastName,
        String fileName,
        List<ContactCourse> courses
) {
    /** {@code role} is the contact's role on that course: INSTRUCTOR or PARTICIPANT. */
    public record ContactCourse(Long courseId, String title, String role) {}
}
