-- sequences
CREATE SEQUENCE IF NOT EXISTS dispatch_batch_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS dispatch_item_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS dispatch_event_seq START WITH 1 INCREMENT BY 1;

-- batch
CREATE TABLE IF NOT EXISTS monthly_dispatch_batch (
                                                      batch_id BIGINT PRIMARY KEY DEFAULT nextval('dispatch_batch_seq'),
    dispatch_type VARCHAR(20) NOT NULL,
    month_ym VARCHAR(7) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100),
    CONSTRAINT uk_dispatch_batch_type_month UNIQUE (dispatch_type, month_ym)
    );

CREATE INDEX IF NOT EXISTS ix_dispatch_batch_month ON monthly_dispatch_batch(month_ym);
CREATE INDEX IF NOT EXISTS ix_dispatch_batch_type ON monthly_dispatch_batch(dispatch_type);

-- item
CREATE TABLE IF NOT EXISTS monthly_dispatch_item (
                                                     item_id BIGINT PRIMARY KEY DEFAULT nextval('dispatch_item_seq'),
    batch_id BIGINT NOT NULL,
    building_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_note VARCHAR(1000),
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(100),
    CONSTRAINT fk_dispatch_item_batch FOREIGN KEY (batch_id) REFERENCES monthly_dispatch_batch(batch_id),
    CONSTRAINT fk_dispatch_item_building FOREIGN KEY (building_id) REFERENCES building(building_id),
    CONSTRAINT uk_dispatch_item_batch_building UNIQUE (batch_id, building_id)
    );

CREATE INDEX IF NOT EXISTS ix_dispatch_item_batch ON monthly_dispatch_item(batch_id);
CREATE INDEX IF NOT EXISTS ix_dispatch_item_building ON monthly_dispatch_item(building_id);
CREATE INDEX IF NOT EXISTS ix_dispatch_item_status ON monthly_dispatch_item(status);

-- event (timeline)
CREATE TABLE IF NOT EXISTS monthly_dispatch_event (
                                                      event_id BIGINT PRIMARY KEY DEFAULT nextval('dispatch_event_seq'),
    item_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    note VARCHAR(2000),
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100),
    CONSTRAINT fk_dispatch_event_item FOREIGN KEY (item_id) REFERENCES monthly_dispatch_item(item_id)
    );

CREATE INDEX IF NOT EXISTS ix_dispatch_event_item ON monthly_dispatch_event(item_id);
CREATE INDEX IF NOT EXISTS ix_dispatch_event_created_at ON monthly_dispatch_event(created_at);

