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

insert into pt_rules (tid, code, name, scope_type, scope_code, rule_type, priority, record_status, expression_language, expression, message)
select t.id,
       'PAX_AGE_LIMIT',
       'Возраст застрахованного до 75 лет',
       'PRODUCT',
       'AIR_PAX_COMBO',
       'PRE_QUOTE_VALIDATION',
       100,
       'ACTIVE',
       'CEL',
       'io_age <= 75',
       'Возраст не должен превышать 75 лет'
from acc_tenants t
where t.code = 'demo'
  and not exists (
      select 1 from pt_rules r
      where r.tid = t.id and r.code = 'PAX_AGE_LIMIT' and r.record_status = 'ACTIVE'
  );
