package com.langia.service.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.entity.MessageLog;
import com.langia.entity.Student;
import com.langia.repository.MessageLogRepository;
import com.langia.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;

@Service
@RequiredArgsConstructor
public class WebhookProcessor {

    private final MessageLogRepository logRepo;
    private final StudentRepository studentRepo;
    private final ObjectMapper objectMapper;
    private final WhatsAppSender whatsappSender;

    /** Compatível com o seu WebhookController: recebe raw JSON como String */
    @Transactional
    public void handleIncoming(String rawBody) {
        try {
            JsonNode body = objectMapper.readTree(rawBody);
            process(body);
        } catch (Exception e) {
            // log mínimo de erro como IN/RECEIVED com payload cru
            MessageLog log = new MessageLog();
            log.setDirection(MessageLog.MsgDirection.IN);
            log.setPayloadJson(rawBody);
            log.setStatus(MessageLog.MsgStatus.RECEIVED);
            logRepo.save(log);
            throw new RuntimeException("Falha ao parsear webhook do WhatsApp", e);
        }
    }

    /** Processamento real do webhook (JSON já parseado) */
    @Transactional
    public void process(JsonNode body) {
        // Persistir log IN
        MessageLog log = new MessageLog();
        log.setDirection(MessageLog.MsgDirection.IN);
        Long studentId = resolveStudentId(body);
        log.setStudentId(studentId);
        try {
            log.setPayloadJson(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.setPayloadJson(body.toString());
        }
        log.setStatus(MessageLog.MsgStatus.RECEIVED);
        logRepo.save(log);

        // Regras simples: OK / PARAR
        String from = json(body, "/entry/0/changes/0/value/messages/0/from");
        String type = json(body, "/entry/0/changes/0/value/messages/0/type");
        if (!"text".equalsIgnoreCase(type)) return;

        String text = json(body, "/entry/0/changes/0/value/messages/0/text/body");
        text = normalize(text);
        String toPhone = from != null ? "+" + from : null;

        if ("ok".equals(text) && toPhone != null) {
            whatsappSender.sendText(toPhone, "Conexão estabelecida ✅");
        } else if ("parar".equals(text) && toPhone != null) {
            studentRepo.findByPhone(toPhone).ifPresent(s -> {
                s.setActive(false);
                studentRepo.save(s);
            });
            whatsappSender.sendText(toPhone, "Ok, você foi desativado. Envie VOLTAR para reativar.");
        }
    }

    private Long resolveStudentId(JsonNode body) {
        String from = json(body, "/entry/0/changes/0/value/messages/0/from");
        if (from == null) return null;
        return studentRepo.findByPhone("+" + from).map(Student::getId).orElse(null);
    }

    private String json(JsonNode node, String ptr) {
        JsonNode n = node.at(ptr);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD);
        return t.replaceAll("\\p{M}+", "");
    }
}
