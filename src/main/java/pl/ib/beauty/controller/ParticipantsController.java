package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.UserMapper;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.UserDtoResponse;
import pl.ib.beauty.service.CourseService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/participants", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantsController {
    private final CourseService courseService;
    private final UserMapper userMapper;

    @GetMapping("{courseId}")
    public List<UserDtoResponse> getParticipants(@PathVariable Long courseId) {
        List<User> courseParticipants = courseService.getCourseParticipants(courseId);
        return userMapper.userToDtoList(courseParticipants);
    }
}
