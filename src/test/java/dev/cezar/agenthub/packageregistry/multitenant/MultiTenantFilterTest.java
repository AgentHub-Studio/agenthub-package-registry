package dev.cezar.agenthub.packageregistry.multitenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MultiTenantFilterTest {

    private final MultiTenantFilter filter = new MultiTenantFilter();

    // A minimal JWT with iss = http://keycloak/realms/my-tenant and sub = user-123
    // Header: {"alg":"none"} Payload: {"iss":"http://keycloak/realms/my-tenant","sub":"user-123"}
    private static final String FAKE_JWT = "eyJhbGciOiJub25lIn0." +
            "eyJpc3MiOiJodHRwOi8va2V5Y2xvYWsvcmVhbG1zL215LXRlbmFudCIsInN1YiI6InVzZXItMTIzIn0.";

    @Test
    void shouldPropagateTenanIdInContextWhenBearerTokenPresent() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/packages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + FAKE_JWT)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> capturedTenantId = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedTenantId.set(ctx.getOrDefault("tenantId", null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(capturedTenantId.get()).isEqualTo("my-tenant");
    }

    @Test
    void shouldContinueWithDefaultSchemaWhenNoBearerToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/packages/public").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> capturedSchema = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedSchema.set(ctx.getOrDefault("schema", null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(capturedSchema.get()).isEqualTo(MultiTenant.DEFAULT_SCHEMA);
    }
}
