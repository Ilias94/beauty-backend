package pl.ib.beauty.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.ib.beauty.model.dao.Category;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDto {
    private Long id;
    private String title;
    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int maxParticipants;
    private Long creatorId;
    private AddressDto address;
    private Category category;
}
