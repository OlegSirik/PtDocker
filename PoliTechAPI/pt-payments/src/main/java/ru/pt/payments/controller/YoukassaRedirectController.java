package ru.pt.payments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Контроллер, на который возвращается пользователь после успешной оплаты
 * TODO проверить работает или нет
 */
@RestController
@RequestMapping("/api/v1/payments/youkassa")
public class YoukassaRedirectController {

    private static final Logger log = LoggerFactory.getLogger(YoukassaRedirectController.class);

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirect(@RequestParam("successTarget") String successTarget,
                                         @RequestParam(value = "failTarget", required = false) String failTarget,
                                         @RequestParam Map<String, String> allParams) {
        if (!StringUtils.hasText(successTarget)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "successTarget parameter is required");
        }

        Map<String, String> paramsToForward = new LinkedHashMap<>(allParams);
        paramsToForward.remove("successTarget");
        paramsToForward.remove("failTarget");

        String paymentStatus = paramsToForward.getOrDefault("payment_status",
                paramsToForward.getOrDefault("status", null));
        boolean success = !StringUtils.hasText(paymentStatus)
                || "succeeded".equalsIgnoreCase(paymentStatus)
                || "waiting_for_capture".equalsIgnoreCase(paymentStatus);

        String target = success ? successTarget : (StringUtils.hasText(failTarget) ? failTarget : successTarget);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        paramsToForward.forEach((key, value) -> {
            if (StringUtils.hasText(value)) {
                builder.queryParam(key, value);
            }
        });

        URI location = builder.build(true).toUri();
        log.debug("Redirecting YooKassa callback to {}", location);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

