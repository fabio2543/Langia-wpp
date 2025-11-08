package com.langia.service.whatsapp;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WhatsAppClient {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.token}")
    private String token;

    private String endpoint() {
        Assert.hasText(phoneNumberId, "whatsapp.phone-number-id não configurado");
        return "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages";
    }

    public void sendTemplate(String toPhoneE164, String templateName, String languageCode) {
        Map<String, Object> body = Map.of(
            "messaging_product", "whatsapp",
            "to", toPhoneE164,
            "type", "template",
            "template", Map.of(
                "name", templateName,
                "language", Map.of("code", languageCode)
            )
        );
        postJson(body);
    }

    public void sendText(String toPhoneE164, String bodyText) {
        Map<String, Object> body = Map.of(
            "messaging_product", "whatsapp",
            "to", toPhoneE164,
            "type", "text",
            "text", Map.of("body", bodyText)
        );
        postJson(body);
    }

    private void postJson(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        // Sem esperar corpo na resposta:
        restTemplate.postForEntity(endpoint(), entity, Void.class);
    }
}
