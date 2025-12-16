package ru.pt.process.gates.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import ru.pt.api.service.process.ProcessOrchestrator;

@Component
public class VskPaymentJmsListener {

    private final Logger logger = LoggerFactory.getLogger(VskPaymentJmsListener.class);

    private final ProcessOrchestrator processOrchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VskPaymentJmsListener(ProcessOrchestrator processOrchestrator) {
        this.processOrchestrator = processOrchestrator;
    }

    // TODO раскомментировать только когда будет брокер
    // @JmsListener(destination = "partapi-platform.payment.sync.queue")
    public void listen(Message message) throws JMSException, JsonProcessingException {
        String response = ((TextMessage) message).getText();
        CallbackData callbackData = objectMapper.readValue(response, CallbackData.class);
        if (callbackData.isTimeout() || !callbackData.getErrorMessage().isEmpty()) {
            logger.error("Exception in jms callback listener - {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(callbackData));
        }
        processOrchestrator.paymentCallback(callbackData.getDraftId());
    }


    static class CallbackData {
        private boolean isTimeout;
        private String errorMessage;
        private String comment;
        private String draftId;
        private String camundaBusinessKey;

        public CallbackData() {
        }

        public CallbackData(boolean isTimeout, String errorMessage, String comment, String draftId, String camundaBusinessKey) {
            this.isTimeout = isTimeout;
            this.errorMessage = errorMessage;
            this.comment = comment;
            this.draftId = draftId;
            this.camundaBusinessKey = camundaBusinessKey;
        }

        public boolean isTimeout() {
            return isTimeout;
        }

        public void setTimeout(boolean timeout) {
            isTimeout = timeout;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getDraftId() {
            return draftId;
        }

        public void setDraftId(String draftId) {
            this.draftId = draftId;
        }

        public String getCamundaBusinessKey() {
            return camundaBusinessKey;
        }

        public void setCamundaBusinessKey(String camundaBusinessKey) {
            this.camundaBusinessKey = camundaBusinessKey;
        }
    }
}
