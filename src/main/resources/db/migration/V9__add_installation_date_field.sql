ALTER TABLE meter
    ADD COLUMN IF NOT EXISTS installation_date DATE;

ALTER TABLE water_meter
    ADD COLUMN IF NOT EXISTS installation_date DATE;
