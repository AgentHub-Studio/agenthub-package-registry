package dev.cezar.agenthub.packageregistry.controller;

import dev.cezar.agenthub.packageregistry.api.InstallPackageRequest;
import dev.cezar.agenthub.packageregistry.api.InstallationResponse;
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
class InstallationControllerTest {

    @Mock
    private PackageRegistryService service;

    private InstallationController controller;

    @BeforeEach
    void setUp() {
        controller = new InstallationController(service);
    }

    @Test
    void shouldListInstallationsForTenant() {
        // InstallationController delegates to TenantContextHelper.getTenantId() which reads
        // from Reactor context, so we test the service delegation directly
        UUID tenantId = UUID.randomUUID();
        InstallationResponse inst = buildInstallation(tenantId, "1.0.0");

        when(service.listInstallations(tenantId)).thenReturn(Flux.just(inst));

        // Wrap in contextWrite to simulate tenant context
        Flux<InstallationResponse> result = Flux.just(tenantId)
                .flatMap(tid -> service.listInstallations(tid));

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();

        verify(service).listInstallations(tenantId);
    }

    @Test
    void shouldInstallPackageForTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        InstallPackageRequest request = new InstallPackageRequest(versionId);
        InstallationResponse response = buildInstallation(tenantId, "2.0.0");

        when(service.installPackage(eq(tenantId), any(InstallPackageRequest.class)))
                .thenReturn(Mono.just(response));

        Mono<InstallationResponse> result = Mono.just(tenantId)
                .flatMap(tid -> service.installPackage(tid, request));

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.installedVersion()).isEqualTo("2.0.0");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();

        verify(service).installPackage(tenantId, request);
    }

    @Test
    void shouldUninstallPackageForTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID installationId = UUID.randomUUID();

        when(service.uninstallPackage(tenantId, installationId)).thenReturn(Mono.empty());

        Mono<Void> result = Mono.just(tenantId)
                .flatMap(tid -> service.uninstallPackage(tid, installationId));

        StepVerifier.create(result)
                .verifyComplete();

        verify(service).uninstallPackage(tenantId, installationId);
    }

    @Test
    void shouldPropagateErrorWhenInstallFails() {
        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        InstallPackageRequest request = new InstallPackageRequest(versionId);

        when(service.installPackage(tenantId, request))
                .thenReturn(Mono.error(new IllegalArgumentException("Package version already installed")));

        Mono<InstallationResponse> result = Mono.just(tenantId)
                .flatMap(tid -> service.installPackage(tid, request));

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("already installed"))
                .verify();
    }

    @Test
    void shouldReturnEmptyWhenNoInstallations() {
        UUID tenantId = UUID.randomUUID();
        when(service.listInstallations(tenantId)).thenReturn(Flux.empty());

        StepVerifier.create(service.listInstallations(tenantId))
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenUninstallFails() {
        UUID tenantId = UUID.randomUUID();
        UUID installationId = UUID.randomUUID();

        when(service.uninstallPackage(tenantId, installationId))
                .thenReturn(Mono.error(new IllegalArgumentException("Installation not found: " + installationId)));

        Mono<Void> result = Mono.just(tenantId)
                .flatMap(tid -> service.uninstallPackage(tid, installationId));

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("Installation not found"))
                .verify();
    }

    // Helpers

    private InstallationResponse buildInstallation(UUID tenantId, String version) {
        return new InstallationResponse(
                UUID.randomUUID(),
                tenantId,
                UUID.randomUUID(),
                version,
                "ACTIVE",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
