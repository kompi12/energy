ALTER TABLE measurement
    ADD CONSTRAINT uq_measurement_meter_date UNIQUE (meter_id, measure_date);