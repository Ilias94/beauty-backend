package pl.ib.beauty.model.dto;

import lombok.*;
import org.springframework.data.history.RevisionMetadata;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoResponse extends AuditableDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer revisionNumber;
    private RevisionMetadata.RevisionType revisionType;
    private List<String> roles;
    private boolean isTeacher;
    private String fileName;
    private List<Long> ownedCourseIds;
}
