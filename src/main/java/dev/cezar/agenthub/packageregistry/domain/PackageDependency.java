package dev.cezar.agenthub.packageregistry.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Table("package_dependency")
public class PackageDependency {

    @Id
    private UUID id;

    @Column("package_version_id")
    private UUID packageVersionId;

    @Column("dependency_type")
    private String dependencyType;

    @Column("dependency_slug")
    private String dependencySlug;

    @Column("dependency_version")
    private String dependencyVersion;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
