package dev.cezar.agenthub.packageregistry.multitenant;

import reactor.core.publisher.Mono;

import java.util.UUID;

public class TenantContextHelper {

    private TenantContextHelper() {}

    public static Mono<UUID> getTenantId() {
        return Mono.deferContextual(ctx -> {
            String tenantId = ctx.getOrDefault("tenantId", null);
            if (tenantId == null) {
                return Mono.error(new IllegalStateException("TenantId not available in context"));
            }
            try {
                return Mono.just(UUID.fromString(tenantId));
            } catch (IllegalArgumentException e) {
                return Mono.error(new IllegalStateException("Invalid tenantId: " + tenantId));
            }
        });
    }

    public static Mono<UUID> getUserId() {
        return Mono.deferContextual(ctx -> {
            String userId = ctx.getOrDefault("userId", null);
            if (userId == null) {
                return Mono.error(new IllegalStateException("UserId not available in context"));
            }
            try {
                return Mono.just(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                return Mono.error(new IllegalStateException("Invalid userId: " + userId));
            }
        });
    }
}
