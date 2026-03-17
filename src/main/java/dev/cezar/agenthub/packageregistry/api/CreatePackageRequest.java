package dev.cezar.agenthub.packageregistry.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePackageRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 200) @Pattern(regexp = "^[a-z0-9_-]+$") String slug,
    @Size(max = 100) String namespace,
    @NotBlank String packageType,
    String description,
    String visibility,
    String provider,
    String repositoryUrl,
    String documentationUrl
) {}
