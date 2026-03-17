package dev.cezar.agenthub.packageregistry.api;

import dev.cezar.agenthub.packageregistry.domain.TenantPackageInstallation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InstallationResponse(
    UUID id,
    UUID tenantId,
    UUID packageVersionId,
    String installedVersion,
    String status,
    OffsetDateTime installedAt,
    OffsetDateTime updatedAt
) {
    public static InstallationResponse from(TenantPackageInstallation i) {
        return new InstallationResponse(
            i.getId(), i.getTenantId(), i.getPackageVersionId(),
            i.getInstalledVersion(), i.getStatus(),
            i.getInstalledAt(), i.getUpdatedAt()
        );
    }
}
