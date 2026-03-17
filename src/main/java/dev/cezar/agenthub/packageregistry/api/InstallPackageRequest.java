package dev.cezar.agenthub.packageregistry.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InstallPackageRequest(
    @NotNull UUID packageVersionId
) {}
