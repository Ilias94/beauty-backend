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
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank(groups = Create.class)
    @Length(min = 5, max = 255, groups = Create.class)
    private String password;
    private String confirmPassword;
    private Integer revisionNumber;
    private RevisionMetadata.RevisionType revisionType;
    private List<String> roles;
    private boolean isTeacher;
    private String fileName;
}
