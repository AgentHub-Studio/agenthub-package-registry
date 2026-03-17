package dev.cezar.agenthub.packageregistry.controller;

import dev.cezar.agenthub.packageregistry.api.*;
import dev.cezar.agenthub.packageregistry.service.PackageRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "Package Registry", description = "Management of distributable packages")
public class PackageController {

    private final PackageRegistryService service;

    @GetMapping
    @Operation(summary = "List all packages")
    public Flux<PackageResponse> listPackages() {
        return service.listPackages();
    }

    @GetMapping("/public")
    @Operation(summary = "List public packages")
    public Flux<PackageResponse> listPublicPackages() {
        return service.listPublicPackages();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package by ID")
    public Mono<PackageResponse> getPackage(@PathVariable UUID id) {
        return service.getPackage(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Package not found: " + id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get package by slug")
    public Mono<PackageResponse> getPackageBySlug(@PathVariable String slug) {
        return service.getPackageBySlug(slug)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Package not found: " + slug)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Create package")
    public Mono<PackageResponse> createPackage(@Valid @RequestBody CreatePackageRequest request) {
        return service.createPackage(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Update package")
    public Mono<PackageResponse> updatePackage(@PathVariable UUID id, @RequestBody UpdatePackageRequest request) {
        return service.updatePackage(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Delete package")
    public Mono<Void> deletePackage(@PathVariable UUID id) {
        return service.deletePackage(id);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "List package versions")
    public Flux<PackageVersionResponse> listVersions(@PathVariable UUID id) {
        return service.listVersions(id);
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(summary = "Get specific version")
    public Mono<PackageVersionResponse> getVersion(@PathVariable UUID id, @PathVariable String version) {
        return service.getVersion(id, version)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Version not found")));
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Publish new version")
    public Mono<PackageVersionResponse> publishVersion(
            @PathVariable UUID id,
            @Valid @RequestBody PublishVersionRequest request,
            Authentication auth) {
        UUID publishedBy = UUID.fromString(auth.getName());
        return service.publishVersion(id, request, publishedBy);
    }
}
