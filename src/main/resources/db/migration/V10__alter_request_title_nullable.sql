-- Alter request_table to make title column nullable
-- This allows check-in/check-out requests to have optional titles
-- Compatible with both PostgreSQL and H2

ALTER TABLE request_table ALTER COLUMN title DROP NOT NULL;
