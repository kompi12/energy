-- sequences (PostgreSQL)
CREATE SEQUENCE IF NOT EXISTS building_address_seq;
CREATE SEQUENCE IF NOT EXISTS person_seq;
CREATE SEQUENCE IF NOT EXISTS apartment_seq;
CREATE SEQUENCE IF NOT EXISTS building_seq;
CREATE SEQUENCE IF NOT EXISTS city_seq;

-- building_address (ako ne postoji)
CREATE TABLE IF NOT EXISTS building_address (
                                                address_id BIGINT PRIMARY KEY DEFAULT nextval('building_address_seq'),
    building_id BIGINT NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20),
    city VARCHAR(100),
    country VARCHAR(100),
    CONSTRAINT fk_building_address_building FOREIGN KEY (building_id) REFERENCES building(building_id)
    );

CREATE INDEX IF NOT EXISTS ix_building_address_building ON building_address(building_id);
CREATE INDEX IF NOT EXISTS ix_building_address_city ON building_address(city);
CREATE INDEX IF NOT EXISTS ix_building_address_postal_code ON building_address(postal_code);

-- person (ako ne postoji)
CREATE TABLE IF NOT EXISTS person (
                                      person_id BIGINT PRIMARY KEY DEFAULT nextval('person_seq'),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    contact VARCHAR(255)
    );
CREATE INDEX IF NOT EXISTS ix_person_last_first ON person(last_name, first_name);

-- apartment.person_id fk (ako ne postoji kolona)
ALTER TABLE apartment
    ADD COLUMN IF NOT EXISTS person_id BIGINT;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_apartment_person'
  ) THEN
ALTER TABLE apartment
    ADD CONSTRAINT fk_apartment_person FOREIGN KEY (person_id) REFERENCES person(person_id);
END IF;
END$$;

CREATE INDEX IF NOT EXISTS ix_apartment_person ON apartment(person_id);
