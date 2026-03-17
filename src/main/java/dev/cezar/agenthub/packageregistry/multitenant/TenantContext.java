package dev.cezar.agenthub.packageregistry.multitenant;

import lombok.Getter;

import java.util.Objects;

@Getter
public class TenantContext {

    private final String tenantId;
    private final String userId;
    private String schemaName;

    public TenantContext(String tenantId) {
        this(tenantId, null);
    }

    public TenantContext(String tenantId, String userId) {
        this.tenantId = tenantId;
        this.userId = userId;
    }

    public String getSchemaName() {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        if (schemaName == null) {
            schemaName = MultiTenant.DEFAULT_SCHEMA.equalsIgnoreCase(tenantId)
                    ? tenantId
                    : MultiTenant.SCHEMA_PREFIX + tenantId;
        }
        return schemaName;
    }
}
