-- Sequences (allocationSize = 50)
CREATE SEQUENCE city_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE person_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE building_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE apartment_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE meter_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE measurement_seq START WITH 1 INCREMENT BY 50;

-- City
CREATE TABLE city (
                      city_id  BIGINT PRIMARY KEY DEFAULT nextval('city_seq'),
                      name     VARCHAR(120) NOT NULL,
                      country  VARCHAR(2)   NOT NULL
);
CREATE UNIQUE INDEX uk_city_name_country ON city(name, country);
CREATE INDEX ix_city_name ON city(name);

-- Person
CREATE TABLE person (
                        person_id  BIGINT PRIMARY KEY DEFAULT nextval('person_seq'),
                        first_name VARCHAR(100) NOT NULL,
                        last_name  VARCHAR(100),
                        contact    VARCHAR(255)
);
CREATE INDEX ix_person_last_first ON person(last_name, first_name);

-- Building
CREATE TABLE building (
                          building_id BIGINT PRIMARY KEY DEFAULT nextval('building_seq'),
                          code        VARCHAR(255) NOT NULL,
                          address     VARCHAR(255) NOT NULL,
                          city_id     BIGINT       NOT NULL,
                          name        VARCHAR(100),
                          CONSTRAINT fk_building_city FOREIGN KEY (city_id) REFERENCES city(city_id)
);
CREATE UNIQUE INDEX uk_building_code ON building(code);
CREATE INDEX ix_building_city ON building(city_id);
CREATE INDEX ix_building_code ON building(code);

-- Apartment
CREATE TABLE apartment (
                           apartment_id     BIGINT PRIMARY KEY DEFAULT nextval('apartment_seq'),
                           apartment_number VARCHAR(20),
                           building_id      BIGINT NOT NULL,
                           person_id        BIGINT,
                           mbr              VARCHAR(100) NOT NULL,
                           hep_mbr          VARCHAR(100),
                           priority         INTEGER,
                           CONSTRAINT fk_apartment_building FOREIGN KEY (building_id) REFERENCES building(building_id) ON DELETE CASCADE,
                           CONSTRAINT fk_apartment_person   FOREIGN KEY (person_id)   REFERENCES person(person_id)
);
CREATE UNIQUE INDEX uk_apartment_building_number ON apartment(building_id, apartment_number);
CREATE INDEX ix_apartment_building ON apartment(building_id);
CREATE INDEX ix_apartment_person ON apartment(person_id);

-- Meter
CREATE TABLE meter (
                       meter_id     BIGINT PRIMARY KEY DEFAULT nextval('meter_seq'),
                       code         VARCHAR(50) NOT NULL,
                       power        VARCHAR(50),
                       apartment_id BIGINT NOT NULL,
                       CONSTRAINT fk_meter_apartment FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_meter_code ON meter(code);
CREATE INDEX ix_meter_apartment ON meter(apartment_id);

-- Measurement
CREATE TABLE measurement (
                             measurement_id BIGINT PRIMARY KEY DEFAULT nextval('measurement_seq'),
                             meter_id       BIGINT NOT NULL,
                             measure_date   DATE  NOT NULL,
                             value          INTEGER,
                             created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                             created_by     VARCHAR(100),
                             CONSTRAINT fk_measurement_meter FOREIGN KEY (meter_id) REFERENCES meter(meter_id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_measurement_meter_date ON measurement(meter_id, measure_date);
CREATE INDEX ix_measurement_meter ON measurement(meter_id);
CREATE INDEX ix_measurement_date ON measurement(measure_date);

-- Trigger to auto-update updated_at on measurement
CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END;
$$;

CREATE TRIGGER set_measurement_updated_at
    BEFORE UPDATE ON measurement
    FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
