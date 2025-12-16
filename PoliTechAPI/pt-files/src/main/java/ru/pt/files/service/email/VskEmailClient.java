package ru.pt.files.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.email.EmailMessage;

@Component
public class VskEmailClient implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(VskEmailClient.class);
    private static final String EMAIL_GATE = "vsk";

    @Override
    public String getEmailGate() {
        return EMAIL_GATE;
    }

    @Override
    public void sendEmail(EmailMessage message, ClientConfiguration configuration) {
        log.info("VSK email mock: to={}, subject={}, attachments={}",
                message.getTo(),
                message.getSubject(),
                message.getAttachments() == null ? 0 : message.getAttachments().size());
    }
}
