package pl.ib.beauty.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.history.RevisionMetadata;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.validator.PasswordValid;
import pl.ib.beauty.validator.group.Create;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@PasswordValid(groups = Create.class)
public class UserDto extends AuditableDto {
    @Null
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
}
