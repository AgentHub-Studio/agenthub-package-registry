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
@Table("tenant_package_installation")
public class TenantPackageInstallation {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("package_version_id")
    private UUID packageVersionId;

    @Column("installed_version")
    private String installedVersion;

    private String status;

    @Column("installed_at")
    private OffsetDateTime installedAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
