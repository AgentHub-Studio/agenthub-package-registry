-- AgentHub Package Registry — Tenant Schema (ah_{tenantId})
-- Tables in this file do NOT carry tenant_id — isolation is via schema.

-- =============================================================================
-- PACKAGE REGISTRY
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_registry (
    id                  UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(200) NOT NULL UNIQUE,
    namespace           VARCHAR(100),
    package_type        VARCHAR(50)  NOT NULL,
    description         TEXT,
    latest_version      VARCHAR(50),
    visibility          VARCHAR(30)  NOT NULL DEFAULT 'PRIVATE',
    provider            VARCHAR(200),
    repository_url      TEXT,
    documentation_url   TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_package_slug       ON package_registry (slug);
CREATE INDEX IF NOT EXISTS idx_package_type       ON package_registry (package_type);
CREATE INDEX IF NOT EXISTS idx_package_visibility ON package_registry (visibility);
CREATE INDEX IF NOT EXISTS idx_package_namespace  ON package_registry (namespace);

-- =============================================================================
-- PACKAGE VERSION
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_version (
    id               UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_id       UUID        NOT NULL REFERENCES package_registry (id) ON DELETE CASCADE,
    version          VARCHAR(50) NOT NULL,
    manifest_json    JSONB       NOT NULL,
    storage_path     TEXT        NOT NULL,
    checksum         VARCHAR(100) NOT NULL,
    compatibility_json JSONB,
    published_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_by     UUID,
    UNIQUE (package_id, version)
);

CREATE INDEX IF NOT EXISTS idx_pkg_version_package   ON package_version (package_id);
CREATE INDEX IF NOT EXISTS idx_pkg_version_published ON package_version (published_at DESC);

-- =============================================================================
-- PACKAGE DEPENDENCY
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_dependency (
    id                   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_version_id   UUID        NOT NULL REFERENCES package_version (id) ON DELETE CASCADE,
    dependency_type      VARCHAR(50) NOT NULL,
    dependency_slug      VARCHAR(200) NOT NULL,
    dependency_version   VARCHAR(50) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pkg_dep_version ON package_dependency (package_version_id);
CREATE INDEX IF NOT EXISTS idx_pkg_dep_slug    ON package_dependency (dependency_slug);

-- =============================================================================
-- PACKAGE ASSET
-- =============================================================================

CREATE TABLE IF NOT EXISTS package_asset (
    id                 UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_version_id UUID        NOT NULL REFERENCES package_version (id) ON DELETE CASCADE,
    asset_type         VARCHAR(50) NOT NULL,
    asset_path         TEXT        NOT NULL,
    content            TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pkg_asset_version ON package_asset (package_version_id);
CREATE INDEX IF NOT EXISTS idx_pkg_asset_type    ON package_asset (asset_type);
