package com.project.q_authent.services.notificaton_service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sourceEmail;

    /**
     * Handle send email from sourceEmail to toEmail
     * @param to destination email {@link String}
     * @param subject email's subject {@link String}
     * @param content email content, content can be html document
     * @param isHtml if content is html document so this value is true, otherwise false
     * @throws MessagingException ex
     */
    @Async
    public void sendEmail(String to, String subject, String content, Boolean isHtml) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(sourceEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, isHtml);

        mailSender.send(mimeMessage);
    }

    @Async
    public void sendValidationCode(String to, Integer code) throws MessagingException {
        sendEmail(to, "", code.toString(), false);
    }
}