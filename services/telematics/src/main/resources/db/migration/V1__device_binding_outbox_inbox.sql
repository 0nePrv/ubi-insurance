create table device_binding (
    device_id  text        primary key,
    policy_id  uuid        not null,
    bound_at   timestamptz not null default now(),
    unbound_at timestamptz
);
-- Одно активное устройство на полис
create unique index device_binding_active_policy_uq on device_binding (policy_id) where unbound_at is null;

-- Transactional outbox: запись появляется в той же транзакции, что и бизнес-изменение.
create table outbox (
    id             uuid        primary key,
    aggregate_type text        not null,
    aggregate_id   text        not null,               -- ключ партиции Kafka: порядок в рамках агрегата
    event_type     text        not null,
    payload        bytea       not null,               -- protobuf из модуля contracts
    headers        jsonb       not null default '{}',  -- correlation_id, causation_id, traceparent
    created_at     timestamptz not null default now(),
    published_at   timestamptz
);
-- Частичный индекс: релей читает только неопубликованное, индекс не растёт вместе с историей
create index outbox_unpublished_idx on outbox (created_at) where published_at is null;

-- Inbox: повторная доставка того же сообщения упирается в первичный ключ
create table inbox (
    consumer    text        not null,
    message_id  uuid        not null,
    received_at timestamptz not null default now(),
    primary key (consumer, message_id)
);
