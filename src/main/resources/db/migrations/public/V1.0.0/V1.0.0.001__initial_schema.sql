-- AgentHub Package Registry — Public Schema
-- Global distributable package catalog shared across all tenants.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- PACKAGE REGISTRY
-- Top-level catalog of distributable packages. No tenant_id — packages are
-- global and shared (visibility controls discoverability).
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_registry (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255)  NOT NULL,
    slug                VARCHAR(255)  NOT NULL UNIQUE,
    namespace           VARCHAR(100),
    package_type        VARCHAR(100)  NOT NULL,
    description         TEXT,
    latest_version      VARCHAR(50),
    visibility          VARCHAR(50)   NOT NULL DEFAULT 'PRIVATE',
    provider            VARCHAR(255),
    repository_url      TEXT,
    documentation_url   TEXT,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pkg_registry_slug        ON package_registry (slug);
CREATE INDEX IF NOT EXISTS idx_pkg_registry_type        ON package_registry (package_type);
CREATE INDEX IF NOT EXISTS idx_pkg_registry_visibility  ON package_registry (visibility);
CREATE INDEX IF NOT EXISTS idx_pkg_registry_namespace   ON package_registry (namespace);

-- =============================================================================
-- PACKAGE VERSION
-- Immutable snapshot of a package at a specific semantic version.
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_version (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id          UUID        NOT NULL REFERENCES package_registry (id) ON DELETE CASCADE,
    version             VARCHAR(50) NOT NULL,
    manifest_json       JSONB,
    storage_path        TEXT,
    checksum            VARCHAR(255),
    compatibility_json  JSONB,
    published_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_by        UUID,
    UNIQUE (package_id, version)
);

CREATE INDEX IF NOT EXISTS idx_pkg_version_package  ON package_version (package_id);
CREATE INDEX IF NOT EXISTS idx_pkg_version_version  ON package_version (version);

-- =============================================================================
-- PACKAGE DEPENDENCY
-- Dependencies declared by a package version (resolved at install time).
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_dependency (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    package_version_id  UUID          NOT NULL REFERENCES package_version (id) ON DELETE CASCADE,
    dependency_type     VARCHAR(50)   NOT NULL DEFAULT 'required',
    dependency_slug     VARCHAR(255)  NOT NULL,
    dependency_version  VARCHAR(50)   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pkg_dep_version  ON package_dependency (package_version_id);

-- =============================================================================
-- PACKAGE ASSET
-- Files bundled with a package version (READMEs, changelogs, icons, etc.).
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_asset (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    package_version_id  UUID        NOT NULL REFERENCES package_version (id) ON DELETE CASCADE,
    asset_type          VARCHAR(100) NOT NULL,
    asset_path          TEXT,
    content             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pkg_asset_version  ON package_asset (package_version_id);

-- =============================================================================
-- TENANT PACKAGE INSTALLATION
-- Tracks which package versions each tenant has installed.
-- Carries tenant_id (String slug) because it bridges global registry with
-- per-tenant state.
-- =============================================================================

CREATE TABLE IF NOT EXISTS tenant_package_installation (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(100)  NOT NULL,
    package_version_id  UUID          NOT NULL REFERENCES package_version (id),
    installed_version   VARCHAR(50)   NOT NULL,
    status              VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE',
    installed_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, package_version_id)
);

CREATE INDEX IF NOT EXISTS idx_tenant_pkg_tenant   ON tenant_package_installation (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_pkg_version  ON tenant_package_installation (package_version_id);
