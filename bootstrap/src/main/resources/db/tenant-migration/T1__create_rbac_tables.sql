-- T1: Create core RBAC tables in tenant schema
-- Applied to each tenant schema independently.

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    resource   VARCHAR(100) NOT NULL,
    action     VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action)
);

-- Roles table (self-referencing for hierarchy)
CREATE TABLE IF NOT EXISTS roles
(
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL,
    tenant_id      UUID         NOT NULL,
    parent_role_id UUID,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name_tenant UNIQUE (name, tenant_id),
    CONSTRAINT fk_roles_parent FOREIGN KEY (parent_role_id) REFERENCES roles (id)
);

-- Role ↔ Permission join table
CREATE TABLE IF NOT EXISTS role_permissions
(
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Users table
CREATE TABLE IF NOT EXISTS users
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash TEXT         NOT NULL,
    tenant_id     UUID         NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email_tenant UNIQUE (email, tenant_id)
);

-- User ↔ Role join table
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Outbox events table (Outbox Pattern)
CREATE TABLE IF NOT EXISTS outbox_events
(
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    event_type     VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(36)  NOT NULL,
    tenant_id      VARCHAR(36)  NOT NULL,
    correlation_id VARCHAR(36),
    payload        TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    published_at   TIMESTAMPTZ,
    error_message  TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events (status, created_at);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_tenant ON users (tenant_id);

COMMENT ON TABLE outbox_events IS 'Transactional outbox for reliable event publishing to Kafka.';
COMMENT ON TABLE users IS 'Authenticated users within this tenant.';
COMMENT ON TABLE roles IS 'RBAC roles with optional parent hierarchy.';
COMMENT ON TABLE permissions IS 'Fine-grained permissions in RESOURCE:ACTION format.';
