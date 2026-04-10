-- V23: Archive the current physical schema — rename each application table to OLD_<original_name>.
-- Intended for in-place migration: after this runs, V24 can create fresh objects with the original names.
--
-- Notes:
-- * flyway_schema_history is left unchanged so Flyway bookkeeping continues to work.
-- * Views are dropped first (they reference table names and would break after renames).
-- * Only ordinary tables (relkind 'r') in schema public are renamed; sequences and indexes move with their tables.

DROP VIEW IF EXISTS pt_lobs_vw CASCADE;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT c.relname AS tname
        FROM pg_class c
                 JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public'
          AND c.relkind = 'r'
          AND c.relname NOT LIKE 'OLD\_%' ESCAPE '\'
          AND c.relname <> 'flyway_schema_history'
        ORDER BY c.relname
        LOOP
            EXECUTE format('ALTER TABLE %I RENAME TO %I', r.tname, 'OLD_' || r.tname);
        END LOOP;
END
$$;
