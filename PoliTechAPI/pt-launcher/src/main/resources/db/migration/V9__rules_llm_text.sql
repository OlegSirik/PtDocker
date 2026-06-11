alter table pt_rules
    add column if not exists llm_text text;
