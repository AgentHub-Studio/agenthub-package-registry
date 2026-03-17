package dev.cezar.agenthub.packageregistry.api;

import dev.cezar.agenthub.packageregistry.domain.PackageVersion;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PackageVersionResponse(
    UUID id,
    UUID packageId,
    String version,
    String storagePath,
    String checksum,
    OffsetDateTime publishedAt,
    UUID publishedBy
) {
    public static PackageVersionResponse from(PackageVersion v) {
        return new PackageVersionResponse(
            v.getId(), v.getPackageId(), v.getVersion(),
            v.getStoragePath(), v.getChecksum(),
            v.getPublishedAt(), v.getPublishedBy()
        );
    }
}
