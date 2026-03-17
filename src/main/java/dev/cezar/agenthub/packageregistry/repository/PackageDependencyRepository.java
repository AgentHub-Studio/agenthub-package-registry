package dev.cezar.agenthub.packageregistry.repository;

import dev.cezar.agenthub.packageregistry.domain.PackageDependency;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PackageDependencyRepository extends ReactiveCrudRepository<PackageDependency, UUID> {

    Flux<PackageDependency> findByPackageVersionId(UUID packageVersionId);
}
