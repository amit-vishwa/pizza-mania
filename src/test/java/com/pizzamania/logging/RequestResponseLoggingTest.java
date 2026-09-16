package com.pizzamania.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

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

    @Test
    void omitsQueryAndAuthorizationWhilePreservingMaskedPayloads() throws Exception {
        logging.logRepository = mock(LogRepository.class);
        MockHttpServletRequest rawRequest = new MockHttpServletRequest("POST", "/api/auth/products");
        rawRequest.setQueryString("access_token=query-secret&unexpected=another-secret");
        rawRequest.addHeader("Authorization", "Bearer header-secret");
        rawRequest.setContent("{\"password\":\"request-secret\",\"product\":\"Pizza\"}"
                .getBytes(StandardCharsets.UTF_8));
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(rawRequest);
        request.getInputStream().readAllBytes();
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(new MockHttpServletResponse());
        response.setStatus(201);
        response.getOutputStream().write("{\"access_token\":\"response-secret\",\"status\":\"created\"}"
                .getBytes(StandardCharsets.UTF_8));

        logging.afterRequest(request, response);

        ArgumentCaptor<Log> record = ArgumentCaptor.forClass(Log.class);
        verify(logging.logRepository).save(record.capture());
        assertEquals("/api/auth/products", record.getValue().getApiUrl());
        assertEquals(201, record.getValue().getApiStatusCode());
        Blob requestBlob = record.getValue().getRequest();
        JsonNode requestJson = OBJECT_MAPPER.readTree(requestBlob.getBytes(1, (int) requestBlob.length()));
        assertEquals("***Masked sensitive information***", requestJson.get("password").asText());
        assertEquals("Pizza", requestJson.get("product").asText());
        Blob responseBlob = record.getValue().getResponse();
        JsonNode responseJson = OBJECT_MAPPER.readTree(responseBlob.getBytes(1, (int) responseBlob.length()));
        assertEquals("***Masked sensitive information***", responseJson.get("access_token").asText());
        assertEquals("created", responseJson.get("status").asText());
    }

    @Test
    void queryStringCannotSelectAnEndpointForAuditLogging() {
        logging.logRepository = mock(LogRepository.class);
        MockHttpServletRequest rawRequest = new MockHttpServletRequest("GET", "/api/public/products");
        rawRequest.setQueryString("auth=secret");

        logging.afterRequest(new ContentCachingRequestWrapper(rawRequest),
                new ContentCachingResponseWrapper(new MockHttpServletResponse()));

        verifyNoInteractions(logging.logRepository);
    }

    @Test
    void persistenceFailureLogsOnlyFixedWarningWithoutThrowable() {
        logging.logRepository = mock(LogRepository.class);
        doThrow(new IllegalStateException("SQL parameters: password=database-secret"))
                .when(logging.logRepository).save(any(Log.class));
        Logger logger = (Logger) LoggerFactory.getLogger(RequestResponseLogging.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            logging.afterRequest(new ContentCachingRequestWrapper(
                    new MockHttpServletRequest("GET", "/api/auth/products")),
                    new ContentCachingResponseWrapper(new MockHttpServletResponse()));

            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals("API audit record could not be stored", event.getFormattedMessage());
            assertNull(event.getThrowableProxy());
            assertTrue(event.getArgumentArray() == null || event.getArgumentArray().length == 0);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
