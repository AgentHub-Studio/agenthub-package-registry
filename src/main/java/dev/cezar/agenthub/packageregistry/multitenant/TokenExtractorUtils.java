package dev.cezar.agenthub.packageregistry.multitenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenExtractorUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern REALM_PATTERN = Pattern.compile("/realms/([^/]+)");

    private TokenExtractorUtils() {}

    public static String getTenantIdFromToken(String token) {
        try {
            Map<String, Object> payload = decodePayload(token);
            String iss = (String) payload.get("iss");
            if (iss == null) return null;
            Matcher m = REALM_PATTERN.matcher(iss);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserIdFromToken(String token) {
        try {
            Map<String, Object> payload = decodePayload(token);
            return (String) payload.get("sub");
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> decodePayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        return MAPPER.readValue(decoded, Map.class);
    }
}
