-- Create sequence for building_address (same allocation pattern as others)
CREATE SEQUENCE IF NOT EXISTS building_address_seq START WITH 1 INCREMENT BY 50;

-- Create building_address table
CREATE TABLE IF NOT EXISTS building_address (
                                                address_id   BIGINT PRIMARY KEY DEFAULT nextval('building_address_seq'),
    building_id  BIGINT NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    postal_code  VARCHAR(20),
    city         VARCHAR(100),
    country      VARCHAR(100) DEFAULT 'HR',
    CONSTRAINT fk_building_address_building
    FOREIGN KEY (building_id) REFERENCES building(building_id)
    ON DELETE CASCADE
    );

-- Helpful indexes
CREATE INDEX IF NOT EXISTS ix_building_address_building    ON building_address(building_id);
CREATE INDEX IF NOT EXISTS ix_building_address_city        ON building_address(city);
CREATE INDEX IF NOT EXISTS ix_building_address_postal_code ON building_address(postal_code);

-- MIGRATION: move existing building.address into building_address
-- (idempotent: won't duplicate if re-run)
INSERT INTO building_address (building_id, address_line, city, country)
SELECT b.building_id, b.address, c.name, c.country
FROM building b
         JOIN city c ON c.city_id = b.city_id
WHERE b.address IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM building_address ba
    WHERE ba.building_id = b.building_id
      AND lower(ba.address_line) = lower(b.address)
);

-- OPTIONAL CLEANUP:
-- If your Java entities have been updated to remove Building.address,
-- you can drop the column. Leave commented until your code is ready.
-- ALTER TABLE building DROP COLUMN IF EXISTS address;
