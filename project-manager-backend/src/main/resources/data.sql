DELETE FROM projects;
DELETE FROM workspace_memberships;
DELETE FROM workspaces;
DELETE FROM users;

INSERT INTO users (id, email, password, name) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'carlos@test.com',
    '$2a$10$XvDJuuJB197SjnEWXWS6h.aPX6TPOjz.WVqgj82imaRb621o0g7my',
    'Carlos Mendoza'
);

INSERT INTO workspaces (id, name) VALUES
    ('22222222-2222-2222-2222-222222222222', 'Workspace Alfa'),
    ('33333333-3333-3333-3333-333333333333', 'Workspace Beta');

INSERT INTO workspace_memberships (user_id, workspace_id, role) VALUES
    ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'ADMIN'),
    ('11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'LECTOR');

INSERT INTO projects (id, name, description, workspace_id, created_at) VALUES
    ('44444444-4444-4444-4444-444444444444', 'Rediseño de landing', 'Actualizar página principal', '22222222-2222-2222-2222-222222222222', NOW()),
    ('55555555-5555-5555-5555-555555555555', 'Migración a cloud', 'Mover infraestructura a AWS', '22222222-2222-2222-2222-222222222222', NOW());

INSERT INTO projects (id, name, description, workspace_id, created_at) VALUES
    ('66666666-6666-6666-6666-666666666666', 'App móvil v2', 'Segunda versión de la app', '33333333-3333-3333-3333-333333333333', NOW()),
    ('77777777-7777-7777-7777-777777777777', 'Dashboard analítico', 'Panel de métricas en tiempo real', '33333333-3333-3333-3333-333333333333', NOW());