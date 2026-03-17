-- AgentHub Package Registry — Public Schema (shared agenthub schema)
-- Tables in this file carry tenant_id for cross-tenant data.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- TENANT PACKAGE INSTALLATION
-- Tracks which package versions are installed by each tenant.
-- Lives in the shared schema because it references tenants from multiple schemas.
-- =============================================================================

CREATE TABLE IF NOT EXISTS tenant_package_installation (
    id                  UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           UUID        NOT NULL,
    package_version_id  UUID        NOT NULL,
    installed_version   VARCHAR(50) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    installed_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, package_version_id)
);

CREATE INDEX IF NOT EXISTS idx_tenant_pkg_tenant    ON tenant_package_installation (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_pkg_version   ON tenant_package_installation (package_version_id);
CREATE INDEX IF NOT EXISTS idx_tenant_pkg_installed ON tenant_package_installation (installed_at DESC);
