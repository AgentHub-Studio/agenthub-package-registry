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
@Table("package_version")
public class PackageVersion {

    @Id
    private UUID id;

    @Column("package_id")
    private UUID packageId;

    private String version;

    @Column("manifest_json")
    private String manifestJson;

    @Column("storage_path")
    private String storagePath;

    private String checksum;

    @Column("compatibility_json")
    private String compatibilityJson;

    @Column("published_at")
    private OffsetDateTime publishedAt;

    @Column("published_by")
    private UUID publishedBy;
}
