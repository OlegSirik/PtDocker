package ru.pt.process.gates.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.payments.service.youkassa.YoukassaPaymentClient;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/youkassa")
public class YoukassaCallbackController {

    private static final Logger log = LoggerFactory.getLogger(YoukassaCallbackController.class);

    private final ProcessOrchestrator processOrchestrator;
    private final StorageService storageService;

    public YoukassaCallbackController(ProcessOrchestrator processOrchestrator, StorageService storageService) {
        this.processOrchestrator = processOrchestrator;
        this.storageService = storageService;
    }

    /**
     * Endpoint to receive YooKassa payment notifications (webhooks).
     * Payload structure: https://yookassa.ru/developers/api#payments_object
     */
    // TODO проверить на тесте/проде
    //  - пока тестовый вызов
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            Object idValue = payload.get("object") instanceof Map<?, ?> objectNode
                    ? objectNode.get("id")
                    : payload.get("id");

            if (idValue instanceof String paymentId) {
                PolicyData policy = storageService.getPolicyByPaymentOrderId(paymentId);
                // TODO проверить, скорее всего это айди заявки на оплату
                processOrchestrator.paymentCallback(policy.getPolicyId().toString());
                log.info("Processed YooKassa notification for payment {}", paymentId);
            } else {
                log.warn("YooKassa webhook received without payment id: {}", payload);
            }
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Failed to process YooKassa webhook", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

