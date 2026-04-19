create table if not exists pt_lob_calculators (
    id bigint primary key default nextval('pt_seq'),
    tid bigint not null,
    lob_code varchar(30) not null,
    calculator_name varchar(300) not null,
    calculator_id bigint not null,
    calculator_formula_json jsonb not null,
    calculator_json jsonb not null
);

create index if not exists idx_pt_lob_calculators_tid_lob
    on pt_lob_calculators (tid, lob_code);
