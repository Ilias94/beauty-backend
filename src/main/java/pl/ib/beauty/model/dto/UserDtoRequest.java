package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.history.RevisionMetadata;
import pl.ib.beauty.validator.group.Create;

import java.util.List;

@Builder
public record UserDtoRequest(@NotBlank
                             String firstName,
                             @NotBlank
                             String lastName,
                             @NotBlank
                             @Email
                             String email,
                             @NotBlank(groups = Create.class)
                             @Length(min = 5, max = 255, groups = Create.class)
                             String password,
                             String confirmPassword,
                             Integer revisionNumber,
                             RevisionMetadata.RevisionType revisionType,
                             List<String> roles,
                             boolean isTeacher,
                             String fileName) {
}
