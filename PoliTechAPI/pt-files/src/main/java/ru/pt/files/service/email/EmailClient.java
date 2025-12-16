package ru.pt.files.service.email;

import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.email.EmailMessage;

/**
 * Интерфейс для отправки email
 */
public interface EmailClient {
    /**
     * @return шлюз, который используется при отправке
     */
    String getEmailGate();

    /**
     * Отправить письмо на почту
     *
     * @param message       данные письма
     * @param configuration настройки шлюза
     */
    void sendEmail(EmailMessage message, ClientConfiguration configuration);
}
