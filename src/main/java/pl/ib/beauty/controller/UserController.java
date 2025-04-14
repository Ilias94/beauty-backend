package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ib.beauty.mapper.UserMapper;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ChangePasswordRequestDto;
import pl.ib.beauty.model.dto.UserDto;
import pl.ib.beauty.service.UserService;
import pl.ib.beauty.validator.group.Create;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public Page<UserDto> getUsers(@RequestParam int page, @RequestParam int size) {
        return userService.getPage(PageRequest.of(page, size)).map(userMapper::userToDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Create.class)
    public UserDto saveUser(@RequestPart @Valid UserDto user, @RequestPart(required = false) MultipartFile file) {
        return userMapper.userToDto(userService.save(userMapper.userDtoToUser(user), user.isTeacher(), file));
    }


    @PutMapping("{id}")
    @PreAuthorize("isAuthenticated() && (@securityService.hasAccessToUser(#id) || hasAuthority('SCOPE_ADMIN'))")
    public UserDto updateUser(@RequestBody @Valid UserDto user, @PathVariable Long id) {
        return userMapper.userToDto(userService.update(userMapper.userDtoToUser(user), id));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @GetMapping("/current")
    public UserDto getCurrentLoginUser() {
        return userMapper.userToDto(userService.currentLoginUser());
    }

    @GetMapping("{id}")
    public UserDto findUserById(@PathVariable Long id) {
        return userMapper.userToDto(userService.getById(id));
    }

    @PutMapping("{id}/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() && (@securityService.hasAccessToUser(#id) || hasAuthority('SCOPE_ADMIN'))")
    public void changePassword(@RequestBody @Valid ChangePasswordRequestDto passwordRequest) {
        userService.changePassword(passwordRequest);
    }
}
