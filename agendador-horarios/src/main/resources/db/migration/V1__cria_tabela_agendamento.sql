CREATE TABLE agendamento (
    id                     BIGSERIAL    PRIMARY KEY,
    servico                VARCHAR(255) NOT NULL,
    profissional           VARCHAR(255) NOT NULL,
    data_hora_agendamento  TIMESTAMP    NOT NULL,
    cliente                VARCHAR(255) NOT NULL,
    telefone_cliente       VARCHAR(30),
    data_insercao          DATE,
    version                BIGINT,
    CONSTRAINT uk_agendamento_profissional_horario UNIQUE (profissional, data_hora_agendamento)
);

CREATE INDEX idx_agendamento_data_hora ON agendamento (data_hora_agendamento);
