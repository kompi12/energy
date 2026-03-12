UPDATE apartment
SET active = TRUE
WHERE active IS NULL;

ALTER TABLE apartment
    ALTER COLUMN active SET DEFAULT TRUE;
