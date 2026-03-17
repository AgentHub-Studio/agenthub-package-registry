package dev.cezar.agenthub.packageregistry.multitenant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenExtractorUtilsTest {

    // Header: {"alg":"none"} Payload: {"iss":"http://keycloak/realms/my-tenant","sub":"user-123"}
    private static final String VALID_TOKEN = "eyJhbGciOiJub25lIn0." +
            "eyJpc3MiOiJodHRwOi8va2V5Y2xvYWsvcmVhbG1zL215LXRlbmFudCIsInN1YiI6InVzZXItMTIzIn0.";

    @Test
    void shouldExtractTenantIdFromIss() {
        String tenantId = TokenExtractorUtils.getTenantIdFromToken(VALID_TOKEN);
        assertThat(tenantId).isEqualTo("my-tenant");
    }

    @Test
    void shouldExtractUserIdFromSub() {
        String userId = TokenExtractorUtils.getUserIdFromToken(VALID_TOKEN);
        assertThat(userId).isEqualTo("user-123");
    }

    @Test
    void shouldReturnNullForInvalidToken() {
        assertThat(TokenExtractorUtils.getTenantIdFromToken("not.a.token")).isNull();
        assertThat(TokenExtractorUtils.getUserIdFromToken("not.a.token")).isNull();
    }

    @Test
    void shouldReturnNullForMissingIss() {
        // Header: {"alg":"none"} Payload: {"sub":"user-only"}
        String token = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyLW9ubHkifQ.";
        assertThat(TokenExtractorUtils.getTenantIdFromToken(token)).isNull();
    }
}
