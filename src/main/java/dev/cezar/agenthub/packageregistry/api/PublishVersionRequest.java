package dev.cezar.agenthub.packageregistry.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record PublishVersionRequest(
    @NotBlank String version,
    @NotNull Map<String, Object> manifest,
    @NotBlank String storagePath,
    @NotBlank String checksum,
    Map<String, Object> compatibility,
    List<DependencyRequest> dependencies
) {
    public record DependencyRequest(
        @NotBlank String dependencyType,
        @NotBlank String dependencySlug,
        @NotBlank String dependencyVersion
    ) {}
}
