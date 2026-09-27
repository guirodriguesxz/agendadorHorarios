package com.guilhermerodrigues.agendador_horarios.services.exceptions;

import java.time.LocalDateTime;

public class HorarioIndisponivelException extends RuntimeException {
    public HorarioIndisponivelException(String profissional, LocalDateTime dataHora) {
        super("O profissional " + profissional + " já possui agendamento em " + dataHora);
    }
}
