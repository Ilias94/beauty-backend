package pl.ib.beauty.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security permits every request (SecurityConfig), so each endpoint guards itself with @PreAuthorize. This catches
 * a new one that changes data and forgot to: the participant report, categories, FAQ, e-mail templates and quiz
 * questions were all open to anyone, logged in or not, that way.
 */
class EndpointAccessRulesTest {

    /** Meant to be called without logging in; each checks the caller another way. */
    private static final Set<String> PUBLIC_WRITES = Set.of(
            "LoginController.login",            // credentials
            "UserController.saveUser",          // registration
            "PaymentController.webhook",        // Stripe signature
            "ConnectController.webhook");       // Stripe signature

    @Test
    void everyEndpointThatChangesDataHasAnAccessRule() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> unguarded = new ArrayList<>();

        for (var candidate : scanner.findCandidateComponents("pl.ib.beauty")) {
            Class<?> controller = Class.forName(candidate.getBeanClassName());
            boolean classGuarded = controller.isAnnotationPresent(PreAuthorize.class);
            for (Method method : controller.getDeclaredMethods()) {
                boolean writes = AnnotatedElementUtils.hasAnnotation(method, PostMapping.class)
                        || AnnotatedElementUtils.hasAnnotation(method, PutMapping.class)
                        || AnnotatedElementUtils.hasAnnotation(method, PatchMapping.class)
                        || AnnotatedElementUtils.hasAnnotation(method, DeleteMapping.class);
                String name = controller.getSimpleName() + "." + method.getName();
                if (writes && !classGuarded && !method.isAnnotationPresent(PreAuthorize.class) && !PUBLIC_WRITES.contains(name)) {
                    unguarded.add(name);
                }
            }
        }

        assertThat(unguarded).as("endpoints that change data without @PreAuthorize").isEmpty();
    }
}
