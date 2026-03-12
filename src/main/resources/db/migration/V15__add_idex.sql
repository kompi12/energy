-- 1) trigram extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2) Apartment: polja koja tražiš LIKE %q%
CREATE INDEX IF NOT EXISTS ix_apartment_apartment_number_trgm
    ON apartment USING gin (lower(apartment_number) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_apartment_mbr_trgm
    ON apartment USING gin (lower(mbr) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_apartment_hep_mbr_trgm
    ON apartment USING gin (lower(hep_mbr) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_apartment_hep_mbr_water_trgm
    ON apartment USING gin (lower(hep_mbr_water) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_apartment_mjerno_mjesto_trgm
    ON apartment USING gin (lower(mjerno_mjesto) gin_trgm_ops);

-- 3) Person: first/last/contact
CREATE INDEX IF NOT EXISTS ix_person_first_name_trgm
    ON person USING gin (lower(first_name) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_person_last_name_trgm
    ON person USING gin (lower(last_name) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_person_contact_trgm
    ON person USING gin (lower(contact) gin_trgm_ops);

-- 4) Building: code/name (ako tražiš i po tome)
CREATE INDEX IF NOT EXISTS ix_building_code_trgm
    ON building USING gin (lower(code) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_building_name_trgm
    ON building USING gin (lower(name) gin_trgm_ops);

-- 5) City name (ako se traži)
CREATE INDEX IF NOT EXISTS ix_city_name_trgm
    ON city USING gin (lower(name) gin_trgm_ops);

-- 6) Device code only:
CREATE INDEX IF NOT EXISTS ix_water_meter_code_trgm
    ON water_meter USING gin (lower(code) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_meter_code_trgm
    ON meter USING gin (lower(code) gin_trgm_ops);
