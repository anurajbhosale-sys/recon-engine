-- CONCEPT: least privilege. Claude Code's Postgres MCP connects as this role,
-- so it can read data and run EXPLAIN but can never write or drop anything.
-- DEFAULT PRIVILEGES makes tables Flyway creates later readable too.
CREATE ROLE recon_readonly LOGIN PASSWORD 'changeme';
GRANT CONNECT ON DATABASE recon TO recon_readonly;
GRANT USAGE ON SCHEMA public TO recon_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO recon_readonly;
