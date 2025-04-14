package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ib.beauty.exception.InvalidPasswordException;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ChangePasswordRequestDto;
import pl.ib.beauty.repository.RoleRepository;
import pl.ib.beauty.repository.UserRepository;
import pl.ib.beauty.security.SecurityUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailService emailService;
    private final TemplateService templateService;

    @SneakyThrows
    @Transactional
    public User save(User user, boolean isTeacher, MultipartFile file) {
        roleRepository.findByName(isTeacher ? "TEACHER" : "USER").ifPresent(role -> user.setRoleList(Collections.singleton(role)));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        if (file != null) {
            String fileName = "user" + user.getId() + ".jpg";
            fileService.saveFile(fileName, file.getBytes());
            user.setFileName(fileName);
        }
        Map<String, Object> variables = prepareTemplateVariables(user);
        emailService.sendEmailFromTemplate(user.getEmail(), "Account Creation", variables);
        return user;
    }

    public User oauth2Save(User user) {
        roleRepository.findByName("USER").ifPresent(role -> user.setRoleList(Collections.singleton(role)));
        userRepository.save(user);
        Map<String, Object> variables = prepareTemplateVariables(user);
        emailService.sendEmailFromTemplate(user.getEmail(), "Account Creation", variables);
        return user;
    }

    public User getById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> getPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User update(User user, Long id) {
        User userDb = getById(id);
        userDb.setFirstName(user.getFirstName());
        userDb.setLastName(user.getLastName());
        userDb.setEmail(user.getEmail());
        return userDb;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User currentLoginUser() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        System.out.println(userEmail);
        return userRepository.findByEmail(userEmail).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto passwordRequest) {
        User currentLoginUser = currentLoginUser();

        if (!passwordEncoder.matches(passwordRequest.getOldPassword(), currentLoginUser.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmNewPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }

        currentLoginUser.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(currentLoginUser);
    }

    private Map<String, Object> prepareTemplateVariables(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("lastName", user.getLastName());
        variables.put("roleName", user.getRoleList().stream().findFirst().orElseThrow().getName()); // Assuming the user always has one role
        return variables;
    }
}
