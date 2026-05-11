create table if not exists pt_payment_installment (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null,
    policy_id bigint not null,
    installment_nr bigint not null,
    due_date date not null,
    amount numeric(19, 2) not null,
    currency varchar(3) not null default 'RUB',
    status varchar(20) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create unique index if not exists ux_pt_payment_installment_tid_policy_nr
    on pt_payment_installment (tid, policy_id, installment_nr);

create table if not exists pt_payment (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null,
    installment_id bigint not null references pt_payment_installment(id),
    amount numeric(19, 2) not null,
    currency varchar(3) not null default 'RUB',
    method varchar(20) not null,
    status varchar(20) not null,
    operator_id bigint,
    provider_reference varchar(120),
    provider_payload text,
    created_at timestamp not null default current_timestamp,
    paid_at timestamp,
    updated_at timestamp not null default current_timestamp
);

create index if not exists idx_pt_payment_tid_installment
    on pt_payment (tid, installment_id);

create table if not exists pt_payment_allocation (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null,
    payment_id bigint not null references pt_payment(id),
    installment_id bigint not null references pt_payment_installment(id),
    allocated_amount numeric(19, 2) not null,
    installment_balance_after numeric(19, 2) not null,
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_pt_payment_alloc_tid_inst
    on pt_payment_allocation (tid, installment_id);

create table if not exists pt_installment_templates (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null default 0,
    installment_type varchar(30) not null,
    installment_template jsonb not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint ux_pt_installment_templates_tid_type unique (tid, installment_type)
);

insert into pt_installment_templates (tid, installment_type, installment_template) values
(0, 'SINGLE', $j$[{"installment_nr":1,"percent":100,"period_months":12}]$j$::jsonb),
(0, 'ANNUAL', $j$[{"installment_nr":1,"percent":100,"period_months":12}]$j$::jsonb),
(0, 'SEMI_ANNUAL', $j$[{"installment_nr":1,"percent":50,"period_months":6},{"installment_nr":2,"percent":50,"period_months":6}]$j$::jsonb),
(0, 'QUARTERLY', $j$[{"installment_nr":1,"percent":25,"period_months":3},{"installment_nr":2,"percent":25,"period_months":3},{"installment_nr":3,"percent":25,"period_months":3},{"installment_nr":4,"percent":25,"period_months":3}]$j$::jsonb),
(0, 'BI_MONTHLY', $j$[{"installment_nr":1,"percent":16.67,"period_months":2},{"installment_nr":2,"percent":16.67,"period_months":2},{"installment_nr":3,"percent":16.67,"period_months":2},{"installment_nr":4,"percent":16.67,"period_months":2},{"installment_nr":5,"percent":16.67,"period_months":2},{"installment_nr":6,"percent":16.65,"period_months":2}]$j$::jsonb),
(0, 'MONTHLY', $j$[{"installment_nr":1,"percent":8.33,"period_months":1},{"installment_nr":2,"percent":8.33,"period_months":1},{"installment_nr":3,"percent":8.33,"period_months":1},{"installment_nr":4,"percent":8.33,"period_months":1},{"installment_nr":5,"percent":8.33,"period_months":1},{"installment_nr":6,"percent":8.33,"period_months":1},{"installment_nr":7,"percent":8.33,"period_months":1},{"installment_nr":8,"percent":8.33,"period_months":1},{"installment_nr":9,"percent":8.33,"period_months":1},{"installment_nr":10,"percent":8.33,"period_months":1},{"installment_nr":11,"percent":8.33,"period_months":1},{"installment_nr":12,"percent":8.37,"period_months":1}]$j$::jsonb);
