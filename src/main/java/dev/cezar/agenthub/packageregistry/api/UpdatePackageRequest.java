package dev.cezar.agenthub.packageregistry.api;

public record UpdatePackageRequest(
    String name,
    String description,
    String visibility,
    String provider,
    String repositoryUrl,
    String documentationUrl
) {}
