create table if not exists pt_currency_rate (
    id bigint primary key default nextval('pt_seq'),
    tid bigint,
    from_currency varchar(3) not null,
    to_currency varchar(3) not null,
    rate numeric(19, 8) not null,
    valid_from date,
    valid_to date,
    source varchar(100),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    constraint chk_pt_currency_rate_pair check (from_currency <> to_currency),
    constraint chk_pt_currency_rate_dates check (valid_to is null or valid_from is null or valid_to >= valid_from)
);

create index if not exists idx_pt_currency_rate_pair_date
    on pt_currency_rate (from_currency, to_currency, valid_from, valid_to);

create index if not exists idx_pt_currency_rate_tid
    on pt_currency_rate (tid);
