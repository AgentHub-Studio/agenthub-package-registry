package dev.cezar.agenthub.packageregistry.api;

import dev.cezar.agenthub.packageregistry.domain.PackageRegistry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PackageResponse(
    UUID id,
    String name,
    String slug,
    String namespace,
    String packageType,
    String description,
    String latestVersion,
    String visibility,
    String provider,
    String repositoryUrl,
    String documentationUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static PackageResponse from(PackageRegistry pkg) {
        return new PackageResponse(
            pkg.getId(), pkg.getName(), pkg.getSlug(), pkg.getNamespace(),
            pkg.getPackageType(), pkg.getDescription(), pkg.getLatestVersion(),
            pkg.getVisibility(), pkg.getProvider(), pkg.getRepositoryUrl(),
            pkg.getDocumentationUrl(), pkg.getCreatedAt(), pkg.getUpdatedAt()
        );
    }
}
