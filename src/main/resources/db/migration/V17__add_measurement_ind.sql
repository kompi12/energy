-- Vxxx__fix_measurement_sequence.sql

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class
        WHERE relkind = 'S' AND relname = 'measurement_seq'
    ) THEN
CREATE SEQUENCE measurement_seq;
END IF;
END
$$;

ALTER TABLE measurement
    ALTER COLUMN measurement_id SET DEFAULT nextval('measurement_seq');

ALTER SEQUENCE measurement_seq
    OWNED BY measurement.measurement_id;

-- poravnaj sekvencu s postojećim podacima (jako važno!)
SELECT setval(
               'measurement_seq',
               COALESCE((SELECT MAX(measurement_id) FROM measurement), 0) + 1,
               false
       );