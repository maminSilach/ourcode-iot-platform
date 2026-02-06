ALTER TABLE devices
    ADD COLUMN target_version TEXT,
ADD COLUMN idempotence_key TEXT;