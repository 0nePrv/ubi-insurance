-- Event store агрегата Policy
create table policy_events (
    global_position bigserial   primary key,
    stream_id       uuid        not null,                -- policy id
    version         int         not null,                -- версия агрегата
    event_type      text        not null,
    payload         jsonb       not null,                -- внутренние события: jsonb, их удобно читать глазами
    effective_at    timestamptz not null,                -- бизнес-время: когда изменение действует
    recorded_at     timestamptz not null default now(),  -- системное время: когда мы о нём узнали
    unique (stream_id, version)                          -- optimistic concurrency: конкурентный append упадёт
);
-- ВНИМАНИЕ: не публикуйте события в Kafka, читая эту таблицу по global_position.
-- bigserial выдаётся до коммита, и транзакция с меньшим номером может закоммититься позже.
-- Публикация идёт через outbox ниже, запись в него — в той же транзакции, что и append.

-- Transactional outbox: запись появляется в той же транзакции, что и бизнес-изменение.
create table outbox (
    id             uuid        primary key,
    seq            bigserial   not null,
    aggregate_type text        not null,
    aggregate_id   text        not null,               -- ключ партиции Kafka: порядок в рамках агрегата
    event_type     text        not null,
    payload        bytea       not null,               -- protobuf из модуля contracts
    headers        jsonb       not null default '{}',  -- correlation_id, causation_id, traceparent
    created_at     timestamptz not null default now(),
    published_at   timestamptz
);
-- Частичный индекс: релей читает только неопубликованное, индекс не растёт вместе с историей
create index outbox_unpublished_idx on outbox (seq) where published_at is null;

-- Inbox: повторная доставка того же сообщения упирается в первичный ключ
create table inbox (
    consumer    text        not null,
    message_id  uuid        not null,
    received_at timestamptz not null default now(),
    primary key (consumer, message_id)
);

-- Правило «одна машина — один активный полис» (между агрегатами, поэтому отдельная таблица).
-- Пишется в той же транзакции, что и события полиса: строгая гарантия без распределённых транзакций.
create table vehicle_reservation (
    vin         text        primary key,
    policy_id   uuid        not null,
    reserved_at timestamptz not null default now()
);

-- Идемпотентность POST /policies (bind): повтор с тем же ключом возвращает сохранённый ответ
create table idempotency_key (
    key           text        primary key,
    request_hash  text        not null,   -- тот же ключ с другим телом запроса = 422
    status        text        not null,   -- IN_PROGRESS | COMPLETED
    response_code int,
    response_body jsonb,
    created_at    timestamptz not null default now()
);
