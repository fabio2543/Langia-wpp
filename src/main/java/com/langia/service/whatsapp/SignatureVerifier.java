package com.langia.service.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// ✅ logger SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(SignatureVerifier.class);

    @Value("${whatsapp.app-secret:}")
    private String appSecret;

    public boolean isValid(String signatureHeader, byte[] body) {
        try {
            if (signatureHeader == null) return false;
            signatureHeader = signatureHeader.trim();
            if (!signatureHeader.regionMatches(true, 0, "sha256=", 0, 7)) return false;

            String expectedHex = signatureHeader.substring(7).trim().toLowerCase(Locale.ROOT);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(body);

            // logs opcionais de diagnóstico (prefixos)
            String computedHex = bytesToHex(computed);
            log.info("HMAC recv={}  calc={}", expectedHex, computedHex);

                return slowEquals(hexToBytes(expectedHex), computed);
        } catch (Exception e) {
            log.error("Erro HMAC", e);
            return false;
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        int diff = a.length ^ b.length;
        for (int i = 0; i < Math.min(a.length, b.length); i++) diff |= (a[i] ^ b[i]);
        return diff == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        if ((len & 1) != 0) throw new IllegalArgumentException("hex length must be even");
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("invalid hex");
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }
}
