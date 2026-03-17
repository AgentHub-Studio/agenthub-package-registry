package dev.cezar.agenthub.packageregistry.controller;

import dev.cezar.agenthub.packageregistry.api.InstallPackageRequest;
import dev.cezar.agenthub.packageregistry.api.InstallationResponse;
import dev.cezar.agenthub.packageregistry.multitenant.TenantContextHelper;
import dev.cezar.agenthub.packageregistry.service.PackageRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/installations")
@RequiredArgsConstructor
@Tag(name = "Package Installations", description = "Manage package installations for the current tenant")
public class InstallationController {

    private final PackageRegistryService service;

    @GetMapping
    @Operation(summary = "List installations for current tenant")
    public Flux<InstallationResponse> listInstallations() {
        return TenantContextHelper.getTenantId()
                .flatMapMany(service::listInstallations);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Install a package version")
    public Mono<InstallationResponse> installPackage(@Valid @RequestBody InstallPackageRequest request) {
        return TenantContextHelper.getTenantId()
                .flatMap(tenantId -> service.installPackage(tenantId, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Uninstall a package")
    public Mono<Void> uninstallPackage(@PathVariable UUID id) {
        return TenantContextHelper.getTenantId()
                .flatMap(tenantId -> service.uninstallPackage(tenantId, id));
    }
}
