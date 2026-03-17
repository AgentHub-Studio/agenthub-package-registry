package dev.cezar.agenthub.packageregistry.service;

import dev.cezar.agenthub.packageregistry.api.CreatePackageRequest;
import dev.cezar.agenthub.packageregistry.api.InstallPackageRequest;
import dev.cezar.agenthub.packageregistry.domain.PackageRegistry;
import dev.cezar.agenthub.packageregistry.domain.PackageVersion;
import dev.cezar.agenthub.packageregistry.domain.TenantPackageInstallation;
import dev.cezar.agenthub.packageregistry.repository.InstallationRepository;
import dev.cezar.agenthub.packageregistry.repository.PackageDependencyRepository;
import dev.cezar.agenthub.packageregistry.repository.PackageRegistryRepository;
import dev.cezar.agenthub.packageregistry.repository.PackageVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageRegistryServiceTest {

    @Mock private PackageRegistryRepository packageRepository;
    @Mock private PackageVersionRepository versionRepository;
    @Mock private PackageDependencyRepository dependencyRepository;
    @Mock private InstallationRepository installationRepository;

    private PackageRegistryService service;

    @BeforeEach
    void setUp() {
        service = new PackageRegistryService(packageRepository, versionRepository, dependencyRepository, installationRepository);
    }

    @Test
    void shouldListAllPackages() {
        PackageRegistry pkg = buildPackage("my-package", "my-package");
        when(packageRepository.findAll()).thenReturn(Flux.just(pkg));

        StepVerifier.create(service.listPackages())
                .assertNext(r -> assertThat(r.slug()).isEqualTo("my-package"))
                .verifyComplete();

        verify(packageRepository).findAll();
    }

    @Test
    void shouldListPublicPackages() {
        PackageRegistry pkg = buildPackage("public-pkg", "public-pkg");
        pkg.setVisibility("PUBLIC");
        when(packageRepository.findAllByVisibility("PUBLIC")).thenReturn(Flux.just(pkg));

        StepVerifier.create(service.listPublicPackages())
                .assertNext(r -> assertThat(r.visibility()).isEqualTo("PUBLIC"))
                .verifyComplete();
    }

    @Test
    void shouldCreatePackageWhenSlugIsUnique() {
        CreatePackageRequest request = new CreatePackageRequest(
                "My Package", "my-package", null, "AGENT",
                "A test package", "PUBLIC", null, null, null
        );

        when(packageRepository.existsBySlug("my-package")).thenReturn(Mono.just(false));
        when(packageRepository.save(any(PackageRegistry.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createPackage(request))
                .assertNext(r -> {
                    assertThat(r.name()).isEqualTo("My Package");
                    assertThat(r.slug()).isEqualTo("my-package");
                    assertThat(r.visibility()).isEqualTo("PUBLIC");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectCreateWhenSlugExists() {
        CreatePackageRequest request = new CreatePackageRequest(
                "My Package", "duplicate-slug", null, "SKILL",
                null, null, null, null, null
        );

        when(packageRepository.existsBySlug("duplicate-slug")).thenReturn(Mono.just(true));

        StepVerifier.create(service.createPackage(request))
                .expectErrorMatches(e -> e.getMessage().contains("duplicate-slug"))
                .verify();
    }

    @Test
    void shouldGetPackageById() {
        UUID id = UUID.randomUUID();
        PackageRegistry pkg = buildPackage("pkg", "pkg");
        pkg.setId(id);
        when(packageRepository.findById(id)).thenReturn(Mono.just(pkg));

        StepVerifier.create(service.getPackage(id))
                .assertNext(r -> assertThat(r.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    void shouldListInstallationsByTenant() {
        UUID tenantId = UUID.randomUUID();
        TenantPackageInstallation inst = TenantPackageInstallation.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .packageVersionId(UUID.randomUUID())
                .installedVersion("1.0.0")
                .status("ACTIVE")
                .installedAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        when(installationRepository.findByTenantIdOrderByInstalledAtDesc(tenantId))
                .thenReturn(Flux.just(inst));

        StepVerifier.create(service.listInstallations(tenantId))
                .assertNext(r -> {
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();
    }

    @Test
    void shouldInstallPackageVersion() {
        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        InstallPackageRequest request = new InstallPackageRequest(versionId);

        PackageVersion version = PackageVersion.builder()
                .id(versionId)
                .packageId(UUID.randomUUID())
                .version("1.0.0")
                .storagePath("packages/pkg/1.0.0.ahpkg")
                .checksum("abc123")
                .publishedAt(OffsetDateTime.now())
                .build();

        when(installationRepository.existsByTenantIdAndPackageVersionId(tenantId, versionId))
                .thenReturn(Mono.just(false));
        when(versionRepository.findById(versionId)).thenReturn(Mono.just(version));
        when(installationRepository.save(any(TenantPackageInstallation.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.installPackage(tenantId, request))
                .assertNext(r -> {
                    assertThat(r.packageVersionId()).isEqualTo(versionId);
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectInstallWhenAlreadyInstalled() {
        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        InstallPackageRequest request = new InstallPackageRequest(versionId);

        when(installationRepository.existsByTenantIdAndPackageVersionId(tenantId, versionId))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.installPackage(tenantId, request))
                .expectErrorMatches(e -> e.getMessage().contains("already installed"))
                .verify();
    }

    // Helpers
    private PackageRegistry buildPackage(String name, String slug) {
        return PackageRegistry.builder()
                .id(UUID.randomUUID())
                .name(name)
                .slug(slug)
                .packageType("AGENT")
                .visibility("PRIVATE")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
