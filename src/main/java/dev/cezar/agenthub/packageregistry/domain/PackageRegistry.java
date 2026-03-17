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
@Table("package_registry")
public class PackageRegistry {

    @Id
    private UUID id;

    private String name;
    private String slug;
    private String namespace;

    @Column("package_type")
    private String packageType;

    private String description;

    @Column("latest_version")
    private String latestVersion;

    private String visibility;
    private String provider;

    @Column("repository_url")
    private String repositoryUrl;

    @Column("documentation_url")
    private String documentationUrl;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
