package ufpb.dcx.house.manager.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtUtil(
        ObjectMapper objectMapper,
        @Value("${app.jwt.secret:change-this-secret-before-production}") String secret,
        @Value("${app.jwt.expiration-seconds:604800}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Long userId, String email) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                "sub", userId.toString(),
                "email", email,
                "exp", Instant.now().getEpochSecond() + expirationSeconds
            ));
            String content = header + "." + payload;
            return content + "." + sign(content);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate JWT.", exception);
        }
    }

    public UserPrincipal validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String content = parts[0] + "." + parts[1];
            if (!sign(content).equals(parts[2])) {
                return null;
            }

            Map<String, Object> claims = objectMapper.readValue(base64UrlDecode(parts[1]), CLAIMS_TYPE);
            Number expiration = (Number) claims.get("exp");
            if (expiration == null || expiration.longValue() < Instant.now().getEpochSecond()) {
                return null;
            }

            Long userId = Long.valueOf((String) claims.get("sub"));
            String email = (String) claims.get("email");
            return new UserPrincipal(userId, email);
        } catch (Exception exception) {
            return null;
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return base64UrlEncode(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
