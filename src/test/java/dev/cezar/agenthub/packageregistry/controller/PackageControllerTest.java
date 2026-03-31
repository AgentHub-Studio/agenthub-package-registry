package dev.cezar.agenthub.packageregistry.controller;

import dev.cezar.agenthub.packageregistry.api.CreatePackageRequest;
import dev.cezar.agenthub.packageregistry.api.PackageResponse;
import dev.cezar.agenthub.packageregistry.api.PackageVersionResponse;
import dev.cezar.agenthub.packageregistry.service.PackageRegistryService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageControllerTest {

    @Mock
    private PackageRegistryService service;

    private PackageController controller;

    @BeforeEach
    void setUp() {
        controller = new PackageController(service);
    }

    @Test
    void shouldListAllPackages() {
        PackageResponse pkg1 = buildPackageResponse("pkg-a", "AGENT");
        PackageResponse pkg2 = buildPackageResponse("pkg-b", "SKILL");

        when(service.listPackages()).thenReturn(Flux.just(pkg1, pkg2));

        StepVerifier.create(controller.listPackages())
                .assertNext(r -> assertThat(r.slug()).isEqualTo("pkg-a"))
                .assertNext(r -> assertThat(r.slug()).isEqualTo("pkg-b"))
                .verifyComplete();

        verify(service).listPackages();
    }

    @Test
    void shouldListPublicPackages() {
        PackageResponse pkg = buildPackageResponse("public-pkg", "AGENT");

        when(service.listPublicPackages()).thenReturn(Flux.just(pkg));

        StepVerifier.create(controller.listPublicPackages())
                .assertNext(r -> {
                    assertThat(r.slug()).isEqualTo("public-pkg");
                    assertThat(r.visibility()).isEqualTo("PUBLIC");
                })
                .verifyComplete();

        verify(service).listPublicPackages();
    }

    @Test
    void shouldGetPackageByIdWhenExists() {
        UUID id = UUID.randomUUID();
        PackageResponse pkg = new PackageResponse(
                id, "My Agent", "my-agent", null, "AGENT",
                null, "1.0.0", "PUBLIC", null, null, null,
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(service.getPackage(id)).thenReturn(Mono.just(pkg));

        StepVerifier.create(controller.getPackage(id))
                .assertNext(r -> {
                    assertThat(r.id()).isEqualTo(id);
                    assertThat(r.slug()).isEqualTo("my-agent");
                })
                .verifyComplete();

        verify(service).getPackage(id);
    }

    @Test
    void shouldReturnErrorWhenPackageNotFoundById() {
        UUID id = UUID.randomUUID();
        when(service.getPackage(id)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getPackage(id))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains(id.toString()))
                .verify();
    }

    @Test
    void shouldGetPackageBySlugWhenExists() {
        PackageResponse pkg = buildPackageResponse("my-agent", "AGENT");
        when(service.getPackageBySlug("my-agent")).thenReturn(Mono.just(pkg));

        StepVerifier.create(controller.getPackageBySlug("my-agent"))
                .assertNext(r -> assertThat(r.slug()).isEqualTo("my-agent"))
                .verifyComplete();

        verify(service).getPackageBySlug("my-agent");
    }

    @Test
    void shouldReturnErrorWhenPackageNotFoundBySlug() {
        when(service.getPackageBySlug("not-found")).thenReturn(Mono.empty());

        StepVerifier.create(controller.getPackageBySlug("not-found"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("not-found"))
                .verify();
    }

    @Test
    void shouldCreatePackage() {
        CreatePackageRequest request = new CreatePackageRequest(
                "My Package", "my-package", null, "AGENT",
                "A test package", "PUBLIC", null, null, null
        );

        PackageResponse created = buildPackageResponse("my-package", "AGENT");
        when(service.createPackage(any(CreatePackageRequest.class))).thenReturn(Mono.just(created));

        StepVerifier.create(controller.createPackage(request))
                .assertNext(r -> {
                    assertThat(r.slug()).isEqualTo("my-package");
                    assertThat(r.packageType()).isEqualTo("AGENT");
                })
                .verifyComplete();

        verify(service).createPackage(request);
    }

    @Test
    void shouldDeletePackage() {
        UUID id = UUID.randomUUID();
        when(service.deletePackage(id)).thenReturn(Mono.empty());

        StepVerifier.create(controller.deletePackage(id))
                .verifyComplete();

        verify(service).deletePackage(id);
    }

    @Test
    void shouldListVersionsForPackage() {
        UUID id = UUID.randomUUID();
        PackageVersionResponse v1 = new PackageVersionResponse(
                UUID.randomUUID(), id, "1.0.0",
                "packages/pkg/1.0.0.ahpkg", "abc123",
                OffsetDateTime.now(), UUID.randomUUID()
        );

        when(service.listVersions(id)).thenReturn(Flux.just(v1));

        StepVerifier.create(controller.listVersions(id))
                .assertNext(v -> assertThat(v.version()).isEqualTo("1.0.0"))
                .verifyComplete();

        verify(service).listVersions(id);
    }

    @Test
    void shouldReturnEmptyWhenNoPackages() {
        when(service.listPackages()).thenReturn(Flux.empty());

        StepVerifier.create(controller.listPackages())
                .verifyComplete();
    }

    // Helpers

    private PackageResponse buildPackageResponse(String slug, String type) {
        return new PackageResponse(
                UUID.randomUUID(),
                slug,
                slug,
                null,
                type,
                "Description",
                "1.0.0",
                "PUBLIC",
                null,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
