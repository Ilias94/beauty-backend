package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.RatingMapper;
import pl.ib.beauty.model.dto.RatingDto;
import pl.ib.beauty.service.RatingService;

@RestController
@RequestMapping(value = "/api/v1/rating", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RatingController {
    private final RatingMapper ratingMapper;
    private final RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public RatingDto saveRating(@RequestBody RatingDto ratingDto) {
        return ratingMapper.ratingToRatingDto(ratingService.saveRating(ratingDto.getCourseId(), ratingDto.getValue()));
    }

    @GetMapping("/{courseId}")
    public RatingDto getCurrentUserRatingByCourseId(@PathVariable Long courseId) {
        return ratingMapper.ratingToRatingDto(ratingService.getCurrentUserRatingByCourseId(courseId));
    }
}
