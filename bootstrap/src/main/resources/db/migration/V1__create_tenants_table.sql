-- V1: Create tenants table in public schema
-- This is the global catalog: every tenant is registered here.
-- All other tables live in the tenant-specific schema.

CREATE TABLE IF NOT EXISTS public.tenants
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    schema_name VARCHAR(100) NOT NULL UNIQUE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_tenants PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_tenants_schema_name ON public.tenants (schema_name);
CREATE INDEX IF NOT EXISTS idx_tenants_active ON public.tenants (active);

COMMENT ON TABLE public.tenants IS 'Global tenant catalog. Each tenant maps to a dedicated PostgreSQL schema.';
