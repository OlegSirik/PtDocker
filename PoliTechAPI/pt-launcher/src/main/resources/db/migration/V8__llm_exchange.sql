create table if not exists llm_exchange (
    id                  bigint primary key default nextval('pt_seq'),
    tid                 bigint not null,
    user_account_id     bigint,
    task_type           varchar(32) not null,
    product_id          bigint,
    version_no          bigint,
    product_code        varchar(64),
    provider_code       varchar(32),
    model               varchar(64),
    success             boolean not null default false,
    status              varchar(32) not null,
    prompt_tokens       int,
    completion_tokens   int,
    latency_ms          bigint,
    request             text not null,
    response            text,
    created_at          timestamptz not null default now()
);

create index if not exists idx_llm_exchange_tid_created
    on llm_exchange (tid, created_at desc);

create index if not exists idx_llm_exchange_product
    on llm_exchange (product_id, version_no);
