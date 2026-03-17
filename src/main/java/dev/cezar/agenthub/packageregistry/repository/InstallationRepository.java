package dev.cezar.agenthub.packageregistry.repository;

import dev.cezar.agenthub.packageregistry.domain.TenantPackageInstallation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InstallationRepository extends ReactiveCrudRepository<TenantPackageInstallation, UUID> {

    Flux<TenantPackageInstallation> findByTenantIdOrderByInstalledAtDesc(UUID tenantId);

    Mono<TenantPackageInstallation> findByTenantIdAndPackageVersionId(UUID tenantId, UUID packageVersionId);

    Mono<Boolean> existsByTenantIdAndPackageVersionId(UUID tenantId, UUID packageVersionId);
}
