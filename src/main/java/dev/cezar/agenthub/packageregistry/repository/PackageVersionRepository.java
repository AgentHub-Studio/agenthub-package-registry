package dev.cezar.agenthub.packageregistry.repository;

import dev.cezar.agenthub.packageregistry.domain.PackageVersion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PackageVersionRepository extends ReactiveCrudRepository<PackageVersion, UUID> {

    Flux<PackageVersion> findByPackageIdOrderByPublishedAtDesc(UUID packageId);

    Mono<PackageVersion> findByPackageIdAndVersion(UUID packageId, String version);

    Mono<Boolean> existsByPackageIdAndVersion(UUID packageId, String version);
}
