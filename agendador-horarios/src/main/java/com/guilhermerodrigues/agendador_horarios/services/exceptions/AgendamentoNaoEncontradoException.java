package com.guilhermerodrigues.agendador_horarios.services.exceptions;

import java.time.LocalDateTime;

public class AgendamentoNaoEncontradoException extends RuntimeException {
    public AgendamentoNaoEncontradoException(String cliente, LocalDateTime dataHora) {
        super("Nenhum agendamento encontrado para " + cliente + " em " + dataHora);
    }
}
