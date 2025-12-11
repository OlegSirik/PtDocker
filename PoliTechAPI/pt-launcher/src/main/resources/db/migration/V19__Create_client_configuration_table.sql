-- Create sequence for client configuration table
CREATE SEQUENCE IF NOT EXISTS acc_client_configuration_seq START WITH 1 INCREMENT BY 1;

-- Table that stores per-client payment configuration
CREATE TABLE IF NOT EXISTS acc_client_configuration (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_client_configuration_seq'),
    payment_gate VARCHAR(255),
    send_email_after_buy BOOLEAN DEFAULT FALSE,
    send_sms_after_buy BOOLEAN DEFAULT FALSE,
    pg_agent_number VARCHAR(255),
    pg_client_login VARCHAR(255),
    pg_client_password VARCHAR(255),
    client_employee_email VARCHAR(255)
);

-- Link configuration to client (one-to-one)
ALTER TABLE acc_clients
    ADD COLUMN IF NOT EXISTS client_configuration_id BIGINT;

ALTER TABLE acc_clients
    ADD CONSTRAINT fk_acc_clients_client_configuration
        FOREIGN KEY (client_configuration_id)
        REFERENCES acc_client_configuration(id);

ALTER TABLE acc_clients
    ADD CONSTRAINT uq_acc_clients_client_configuration
        UNIQUE (client_configuration_id);

