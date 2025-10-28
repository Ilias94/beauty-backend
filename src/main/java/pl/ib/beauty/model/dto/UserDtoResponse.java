package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.history.RevisionMetadata;
import pl.ib.beauty.validator.group.Create;

import java.util.List;

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
