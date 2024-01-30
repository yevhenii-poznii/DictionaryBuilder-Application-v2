package com.kiskee.vocabulary.service.email;

import com.kiskee.vocabulary.config.properties.email.EmailContextProperties;
import com.kiskee.vocabulary.exception.email.SendEmailException;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@AllArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailContextProperties emailContext;

    @Async
    @Override
    @Retryable
    public void sendVerificationEmail(UserSecureProjection userInfo, String verificationToken) {
        try {
            sendEmail(userInfo, verificationToken);

            log.info("Verification email has been successfully sent to [{}]", userInfo.getEmail());
        } catch (MessagingException e) {

            log.error("Verification email hasn't been sent due to [{}]", e.getMessage());

            throw new SendEmailException(e.getMessage());
        }

    }

    private void sendEmail(UserSecureProjection userInfo, String verificationToken)
            throws MessagingException {
        String confirmationLink = emailContext.getConfirmationUrl() + verificationToken;

        Context context = buildMessageContext(userInfo.getUsername(), confirmationLink);

        String htmlContent = templateEngine.process(emailContext.getTemplateLocation(), context);

        MimeMessage mimeMessage = prepareMessage(userInfo.getEmail(), htmlContent);

        mailSender.send(mimeMessage);
    }

    private MimeMessage prepareMessage(String userEmail, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        mimeMessageHelper.setFrom(emailContext.getFrom());
        mimeMessageHelper.setTo(userEmail);
        mimeMessageHelper.setSubject(emailContext.getSubject());
        mimeMessageHelper.setText(htmlContent, true);

        return mimeMessage;
    }

    private Context buildMessageContext(String username, String confirmationLink) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("confirmationLink", confirmationLink);

        return context;
    }

}
