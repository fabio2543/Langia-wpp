package com.langia.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.entity.MessageLog;
import com.langia.repository.MessageLogRepository;
import com.langia.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WhatsAppSender {

    private final WhatsAppClient client;
    private final MessageLogRepository logRepo;
    private final StudentRepository studentRepo;
    private final ObjectMapper objectMapper;

    /** H1.2 – Envio do template de boas-vindas */
    @Transactional
    public void sendWelcomeTemplate(String phoneE164) {
        final String template = "welcome_default";
        final String lang = "pt_BR";
        final String payloadJson = """
               {"messaging_product":"whatsapp","to":"%s","type":"template",
                "template":{"name":"%s","language":{"code":"%s"}}}
               """.formatted(phoneE164, template, lang).replaceAll("\\s+","");

        Long studentId = studentRepo.findByPhone(phoneE164).map(s -> s.getId()).orElse(null);

        try {
            client.sendTemplate(phoneE164, template, lang);
            logRepo.save(MessageLog.outSent(studentId, payloadJson));
        } catch (Exception ex) {
            logRepo.save(MessageLog.outError(studentId, payloadJson));
            throw new RuntimeException("Erro ao enviar template WhatsApp", ex);
        }
    }

    /** Suporte ao fluxo “OK/PARAR” no webhook */
    @Transactional
    public void sendText(String phoneE164, String text) {
        String escaped;
        try {
            escaped = objectMapper.writeValueAsString(text); // garante aspas/escape corretos
        } catch (Exception e) {
            escaped = "\"" + text.replace("\"","\\\"") + "\"";
        }
        final String payloadJson = """
            {"messaging_product":"whatsapp","to":"%s","type":"text","text":{"body":%s}}
            """.formatted(phoneE164, escaped).replaceAll("\\s+","");

        Long studentId = studentRepo.findByPhone(phoneE164).map(s -> s.getId()).orElse(null);

        try {
            client.sendText(phoneE164, text);
            logRepo.save(MessageLog.outSent(studentId, payloadJson));
        } catch (Exception ex) {
            logRepo.save(MessageLog.outError(studentId, payloadJson));
            throw new RuntimeException("Erro ao enviar texto WhatsApp", ex);
        }
    }
}
