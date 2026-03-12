create table if not exists app_user (
                                        user_id bigserial primary key,
                                        username varchar(100) not null unique,
    password_hash varchar(255) not null,
    enabled boolean not null default true,
    roles varchar(255) not null default 'USER',
    created_at timestamptz not null default now()
    );


create table if not exists audit_event (
                                           audit_event_id bigserial primary key,
                                           created_at timestamptz not null default now(),
    username varchar(100),
    action varchar(50) not null,
    entity_type varchar(80),
    entity_id varchar(80),
    details jsonb
    );

create index if not exists ix_audit_event_time on audit_event(created_at);
create index if not exists ix_audit_event_user on audit_event(username);
create index if not exists ix_audit_event_entity on audit_event(entity_type, entity_id);
