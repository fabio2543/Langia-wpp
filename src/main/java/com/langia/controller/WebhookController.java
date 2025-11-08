package com.langia.controller;

import com.langia.service.whatsapp.SignatureVerifier;
import com.langia.service.whatsapp.WebhookProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhooks/whatsapp")
public class WebhookController {

     @Value("${whatsapp.verify-token}")
    private String verifyToken;

    private final SignatureVerifier verifier;
    private final WebhookProcessor processor;

    public WebhookController(SignatureVerifier verifier, WebhookProcessor processor) {
        this.verifier = verifier;
        this.processor = processor;
    }

    // GET: verificação do webhook (echo do challenge)
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // POST: recepção do evento (valida HMAC e grava IN/RECEIVED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody byte[] bodyBytes) {

        if (signature == null || !verifier.isValid(signature, bodyBytes)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String rawBody = new String(bodyBytes, StandardCharsets.UTF_8); // para persistir/parsear depois
        processor.handleIncoming(rawBody);
        return ResponseEntity.ok().build();
    }

}
