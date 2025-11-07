package com.langia.service.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.entity.MessageLog;
import com.langia.repository.MessageLogRepository;
import org.springframework.stereotype.Service;

@Service
public class WebhookProcessor {

    private final MessageLogRepository repo;
    private final ObjectMapper mapper;

    public WebhookProcessor(MessageLogRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public void handleIncoming(String rawBody) {
        try {
            JsonNode json = mapper.readTree(rawBody); // <- parse verdadeiro JSON
            MessageLog log = new MessageLog();
            log.setDirection(MessageLog.Direction.IN);
            log.setStatus(MessageLog.Status.RECEIVED);
            log.setPayload(json);                     // <- salva como JSONB
            // opcional: setStudentId(...)
            repo.save(log);
        } catch (Exception e) {
            // trate se quiser marcar como ERROR
            throw new RuntimeException("Invalid webhook payload", e);
        }
    }
}
