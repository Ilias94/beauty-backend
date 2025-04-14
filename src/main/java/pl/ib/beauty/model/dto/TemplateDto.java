package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateDto {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Body cannot be blank")
    private String body;
    @NotBlank(message = "Subject cannot be blank")
    private String subject;
}
