-- T2: Seed default ADMIN role and permissions for a new tenant
-- This script is idempotent (INSERT ... ON CONFLICT DO NOTHING)

INSERT INTO permissions (id, resource, action)
VALUES
    (gen_random_uuid(), 'user', 'read'),
    (gen_random_uuid(), 'user', 'write'),
    (gen_random_uuid(), 'user', 'delete'),
    (gen_random_uuid(), 'role', 'read'),
    (gen_random_uuid(), 'role', 'write'),
    (gen_random_uuid(), 'tenant', 'manage')
ON CONFLICT (resource, action) DO NOTHING;

-- Insert base OPERATOR role
INSERT INTO roles (id, name, tenant_id, parent_role_id)
SELECT gen_random_uuid(), 'OPERATOR', :tenantId, NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'OPERATOR' AND tenant_id = :tenantId);

-- Insert ADMIN role (inherits from OPERATOR)
INSERT INTO roles (id, name, tenant_id, parent_role_id)
SELECT gen_random_uuid(), 'ADMIN', :tenantId, r.id
FROM roles r
WHERE r.name = 'OPERATOR'
  AND r.tenant_id = :tenantId
  AND NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN' AND tenant_id = :tenantId);

-- Grant all permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND r.tenant_id = :tenantId
ON CONFLICT DO NOTHING;
