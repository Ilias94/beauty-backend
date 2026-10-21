package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One photo in a course's gallery; the one at position 0 is the cover. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(schema = "beautypg")
public class CourseImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** S3 key in the public pictures bucket, e.g. courses/16/1f0c…webp. */
    private String fileName;

    private int position;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
