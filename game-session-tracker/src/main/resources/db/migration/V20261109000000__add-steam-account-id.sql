-- The 64-bit Steam id of the account that played the session. NULL for sessions recorded before
-- this was tracked, or when the account could not be determined.
ALTER TABLE gaming_sessions ADD COLUMN steam_account_id INTEGER;
