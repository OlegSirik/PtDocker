create table if not exists pt_rules (
    id                  bigint primary key default nextval('pt_seq'),
    tid                 bigint not null,
    code                varchar(64) not null,
    name                varchar(300) not null,
    scope_type          varchar(16) not null,
    scope_code          varchar(64) not null,
    rule_type           varchar(32) not null,
    priority            int not null default 100,
    record_status       varchar(16) not null default 'ACTIVE',
    expression_language varchar(16) not null default 'CEL',
    expression          text not null,
    message             varchar(500) not null,
    llm_text            text,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    constraint chk_pt_rules_scope check (scope_type in ('PRODUCT','LOB','TENANT','CLIENT')),
    constraint chk_pt_rules_status check (record_status in ('ACTIVE','INACTIVE','DELETED'))
);

create unique index if not exists ux_pt_rules_tid_code_active
    on pt_rules (tid, code)
    where record_status = 'ACTIVE';

create index if not exists idx_pt_rules_lookup
    on pt_rules (tid, rule_type, scope_type, scope_code, record_status, priority);

