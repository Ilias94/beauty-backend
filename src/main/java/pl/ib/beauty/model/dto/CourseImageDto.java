package pl.ib.beauty.model.dto;

/** A gallery photo: {@code fileName} is its key in the public pictures bucket; position 0 is the cover. */
public record CourseImageDto(Long id, String fileName, int position) {
}
