-- Формула тарифа живёт в коде (Kotlin DSL), а числа — здесь.
-- Версия тарифа = версия формулы из кода + версия таблицы коэффициентов.
create table rating_factor_table (
    tariff_code    text        not null,
    table_name     text        not null,   -- например base_rate_by_power, region_coefficient
    version        int         not null,
    effective_from date        not null,
    effective_to   date,                    -- null = действует бессрочно
    rows           jsonb       not null,
    published_at   timestamptz not null default now(),
    primary key (tariff_code, table_name, version)
);
-- Опубликованную версию не меняют: исправление = новая версия с новой датой вступления в силу
