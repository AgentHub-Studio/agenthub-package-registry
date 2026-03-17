package dev.cezar.agenthub.packageregistry.repository;

import dev.cezar.agenthub.packageregistry.domain.PackageRegistry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PackageRegistryRepository extends ReactiveCrudRepository<PackageRegistry, UUID> {

    Mono<PackageRegistry> findBySlug(String slug);

    Flux<PackageRegistry> findAllByVisibility(String visibility);

    Mono<Boolean> existsBySlug(String slug);
}
