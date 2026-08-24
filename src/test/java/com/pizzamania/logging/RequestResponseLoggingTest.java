package com.pizzamania.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class RequestResponseLoggingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final RequestResponseLogging logging = new RequestResponseLogging();

    @Test
    void masksSensitiveFieldsAtEveryNestingLevel() throws Exception {
        String payload = """
                {
                  "userName": "learner@example.com",
                  "userPass": "plain-text-password",
                  "profile": {
                    "jwt_token": "token-value",
                    "displayName": "Learner"
                  },
                  "sessions": [
                    {"accessToken": "access-value", "status": "active"}
                  ]
                }
                """;

        Blob sanitized = logging.sanitizePayload(payload.getBytes(StandardCharsets.UTF_8));
        JsonNode result = OBJECT_MAPPER.readTree(sanitized.getBytes(1, (int) sanitized.length()));

        assertEquals("***Masked sensitive information***", result.get("userPass").asText());
        assertEquals("***Masked sensitive information***", result.at("/profile/jwt_token").asText());
        assertEquals("***Masked sensitive information***", result.at("/sessions/0/accessToken").asText());
        assertEquals("learner@example.com", result.get("userName").asText());
        assertEquals("Learner", result.at("/profile/displayName").asText());
        assertEquals("active", result.at("/sessions/0/status").asText());
    }

    @Test
    void doesNotStorePayloadThatCannotBeSafelyParsed() {
        Blob sanitized = logging.sanitizePayload("not-json password=secret".getBytes(StandardCharsets.UTF_8));

        assertNull(sanitized);
    }
}
