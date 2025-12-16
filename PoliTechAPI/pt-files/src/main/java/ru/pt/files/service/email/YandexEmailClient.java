package ru.pt.files.service.email;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.email.EmailAttachment;
import ru.pt.api.dto.email.EmailMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@Component
public class YandexEmailClient implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(YandexEmailClient.class);
    private static final String EMAIL_GATE = "yandex";

    @Override
    public String getEmailGate() {
        return EMAIL_GATE;
    }

    @Override
    public void sendEmail(EmailMessage message, ClientConfiguration configuration) {
        validate(configuration, message);

        Session session = Session.getInstance(buildProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configuration.getEmailLogin(), configuration.getEmailPassword());
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(configuration.getEmailLogin()));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(message.getTo()));
            mimeMessage.setSubject(nullToEmpty(message.getSubject()), StandardCharsets.UTF_8.name());

            List<EmailAttachment> attachments = message.getAttachments();
            if (attachments == null || attachments.isEmpty()) {
                setBody(mimeMessage, message);
            } else {
                mimeMessage.setContent(buildMultipart(message));
            }

            Transport.send(mimeMessage);
            log.info("Yandex email sent to {}", message.getTo());
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to send email via Yandex", ex);
        }
    }

    private void validate(ClientConfiguration configuration, EmailMessage message) {
        if (configuration == null) {
            throw new IllegalStateException("Email configuration is not provided");
        }
        if (!StringUtils.hasText(configuration.getEmailLogin()) || !StringUtils.hasText(configuration.getEmailPassword())) {
            throw new IllegalStateException("Yandex email credentials are not configured");
        }
        if (message == null || !StringUtils.hasText(message.getTo())) {
            throw new IllegalArgumentException("Recipient email is required");
        }
    }

    private Properties buildProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.yandex.ru");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        return props;
    }

    private void setBody(MimeMessage mimeMessage, EmailMessage message) throws MessagingException {
        if (message.isHtml()) {
            mimeMessage.setContent(nullToEmpty(message.getBody()), "text/html; charset=UTF-8");
        } else {
            mimeMessage.setText(nullToEmpty(message.getBody()), StandardCharsets.UTF_8.name());
        }
    }

    private Multipart buildMultipart(EmailMessage message) throws MessagingException {
        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        if (message.isHtml()) {
            textPart.setContent(nullToEmpty(message.getBody()), "text/html; charset=UTF-8");
        } else {
            textPart.setText(nullToEmpty(message.getBody()), StandardCharsets.UTF_8.name());
        }
        multipart.addBodyPart(textPart);

        for (EmailAttachment attachment : message.getAttachments()) {
            if (attachment == null || attachment.getContent() == null) {
                continue;
            }
            MimeBodyPart attachmentPart = new MimeBodyPart();
            String contentType = StringUtils.hasText(attachment.getContentType())
                    ? attachment.getContentType()
                    : "application/octet-stream";
            DataSource source = new ByteArrayDataSource(attachment.getContent(), contentType);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(
                    StringUtils.hasText(attachment.getFilename()) ? attachment.getFilename() : "attachment"
            );
            multipart.addBodyPart(attachmentPart);
        }

        return multipart;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
