create table if not exists pt_payment_installment (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null,
    policy_id bigint not null,
    installment_nr integer not null,
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

