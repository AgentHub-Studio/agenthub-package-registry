package dev.cezar.agenthub.packageregistry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cezar.agenthub.packageregistry.api.*;
import dev.cezar.agenthub.packageregistry.domain.*;
import dev.cezar.agenthub.packageregistry.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing package registry operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageRegistryService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PackageRegistryRepository packageRepository;
    private final PackageVersionRepository versionRepository;
    private final PackageDependencyRepository dependencyRepository;
    private final InstallationRepository installationRepository;

    // ── Package CRUD ──────────────────────────────────────────────────────────

    public Flux<PackageResponse> listPackages() {
        return packageRepository.findAll()
                .map(PackageResponse::from);
    }

    public Flux<PackageResponse> listPublicPackages() {
        return packageRepository.findAllByVisibility("PUBLIC")
                .map(PackageResponse::from);
    }

    public Mono<PackageResponse> getPackage(UUID id) {
        return packageRepository.findById(id)
                .map(PackageResponse::from);
    }

    public Mono<PackageResponse> getPackageBySlug(String slug) {
        return packageRepository.findBySlug(slug)
                .map(PackageResponse::from);
    }

    public Mono<PackageResponse> createPackage(CreatePackageRequest request) {
        return packageRepository.existsBySlug(request.slug())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Package with slug already exists: " + request.slug()));
                    }
                    PackageRegistry pkg = PackageRegistry.builder()
                            .name(request.name())
                            .slug(request.slug())
                            .namespace(request.namespace())
                            .packageType(request.packageType())
                            .description(request.description())
                            .visibility(request.visibility() != null ? request.visibility() : "PRIVATE")
                            .provider(request.provider())
                            .repositoryUrl(request.repositoryUrl())
                            .documentationUrl(request.documentationUrl())
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return packageRepository.save(pkg);
                })
                .map(PackageResponse::from);
    }

    public Mono<PackageResponse> updatePackage(UUID id, UpdatePackageRequest request) {
        return packageRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Package not found: " + id)))
                .flatMap(pkg -> {
                    if (request.name() != null) pkg.setName(request.name());
                    if (request.description() != null) pkg.setDescription(request.description());
                    if (request.visibility() != null) pkg.setVisibility(request.visibility());
                    if (request.provider() != null) pkg.setProvider(request.provider());
                    if (request.repositoryUrl() != null) pkg.setRepositoryUrl(request.repositoryUrl());
                    if (request.documentationUrl() != null) pkg.setDocumentationUrl(request.documentationUrl());
                    pkg.setUpdatedAt(OffsetDateTime.now());
                    return packageRepository.save(pkg);
                })
                .map(PackageResponse::from);
    }

    public Mono<Void> deletePackage(UUID id) {
        return packageRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Package not found: " + id)))
                .flatMap(pkg -> packageRepository.deleteById(id));
    }

    // ── Package Versions ──────────────────────────────────────────────────────

    public Flux<PackageVersionResponse> listVersions(UUID packageId) {
        return versionRepository.findByPackageIdOrderByPublishedAtDesc(packageId)
                .map(PackageVersionResponse::from);
    }

    public Mono<PackageVersionResponse> getVersion(UUID packageId, String version) {
        return versionRepository.findByPackageIdAndVersion(packageId, version)
                .map(PackageVersionResponse::from);
    }

    public Mono<PackageVersionResponse> publishVersion(UUID packageId, PublishVersionRequest request, UUID publishedBy) {
        return packageRepository.findById(packageId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Package not found: " + packageId)))
                .flatMap(pkg -> versionRepository.existsByPackageIdAndVersion(packageId, request.version())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        "Version already exists: " + request.version()));
                            }
                            PackageVersion version = PackageVersion.builder()
                                    .packageId(packageId)
                                    .version(request.version())
                                    .manifestJson(toJsonString(request.manifest()))
                                    .storagePath(request.storagePath())
                                    .checksum(request.checksum())
                                    .compatibilityJson(request.compatibility() != null ? toJsonString(request.compatibility()) : null)
                                    .publishedAt(OffsetDateTime.now())
                                    .publishedBy(publishedBy)
                                    .build();
                            return versionRepository.save(version)
                                    .flatMap(saved -> saveDependencies(saved, request.dependencies())
                                            .then(updateLatestVersion(pkg, request.version()))
                                            .thenReturn(saved));
                        }))
                .map(PackageVersionResponse::from);
    }

    // ── Installations ─────────────────────────────────────────────────────────

    public Flux<InstallationResponse> listInstallations(UUID tenantId) {
        return installationRepository.findByTenantIdOrderByInstalledAtDesc(tenantId)
                .map(InstallationResponse::from);
    }

    public Mono<InstallationResponse> installPackage(UUID tenantId, InstallPackageRequest request) {
        return installationRepository.existsByTenantIdAndPackageVersionId(tenantId, request.packageVersionId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Package version already installed"));
                    }
                    return versionRepository.findById(request.packageVersionId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "Package version not found: " + request.packageVersionId())))
                            .flatMap(version -> {
                                TenantPackageInstallation installation = TenantPackageInstallation.builder()
                                        .tenantId(tenantId)
                                        .packageVersionId(request.packageVersionId())
                                        .installedVersion(version.getVersion())
                                        .status("ACTIVE")
                                        .installedAt(OffsetDateTime.now())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();
                                return installationRepository.save(installation);
                            });
                })
                .map(InstallationResponse::from);
    }

    public Mono<Void> uninstallPackage(UUID tenantId, UUID installationId) {
        return installationRepository.findById(installationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Installation not found: " + installationId)))
                .flatMap(installation -> {
                    if (!installation.getTenantId().equals(tenantId)) {
                        return Mono.error(new IllegalArgumentException("Installation does not belong to this tenant"));
                    }
                    installation.setStatus("REMOVED");
                    installation.setUpdatedAt(OffsetDateTime.now());
                    return installationRepository.save(installation).then();
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Mono<Void> saveDependencies(PackageVersion version, List<PublishVersionRequest.DependencyRequest> deps) {
        if (deps == null || deps.isEmpty()) return Mono.empty();
        return Flux.fromIterable(deps)
                .map(dep -> PackageDependency.builder()
                        .packageVersionId(version.getId())
                        .dependencyType(dep.dependencyType())
                        .dependencySlug(dep.dependencySlug())
                        .dependencyVersion(dep.dependencyVersion())
                        .createdAt(OffsetDateTime.now())
                        .build())
                .flatMap(dependencyRepository::save)
                .then();
    }

    private Mono<Void> updateLatestVersion(PackageRegistry pkg, String version) {
        pkg.setLatestVersion(version);
        pkg.setUpdatedAt(OffsetDateTime.now());
        return packageRepository.save(pkg).then();
    }

    private static String toJsonString(Object value) {
        if (value == null) return null;
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize value to JSON", e);
        }
    }
}
