package com.guilhermerodrigues.agendador_horarios.controller.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @NotBlank String servico,
        @NotBlank String profissional,
        @NotNull @Future LocalDateTime dataHoraAgendamento,
        @NotBlank String cliente,
        String telefoneCliente
) {
}
