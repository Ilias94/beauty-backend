package pl.ib.beauty.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import pl.ib.beauty.model.dao.Template;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateService templateService;
    private final ITemplateEngine iTemplateEngine;

    @SneakyThrows
    public void sendEmailFromTemplate(String email, String templateName, Map<String, Object> variables) {
        Template template = templateService.findByName(templateName);
        String processedTemplate = iTemplateEngine.process(template.getBody(), new Context(Locale.getDefault(), variables));
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(template.getSubject());
        mimeMessageHelper.setText(processedTemplate, true);
        mimeMessageHelper.setFrom("beauty@ib.beauty.com");
        javaMailSender.send(mimeMessage);
    }
}
