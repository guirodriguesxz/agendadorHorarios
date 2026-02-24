package com.guilhermerodrigues.agendador_horarios.infrastructure.repository;

import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Verifica conflito de horário por serviço
    Agendamento findByServicoAndDataHoraAgendamentoBetween(
            String servico,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca todos os agendamentos do dia
    List<Agendamento> findByDataHoraAgendamentoBetween(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca agendamento específico
    Agendamento findByDataHoraAgendamentoAndCliente(
            LocalDateTime dataHoraAgendamento,
            String cliente
    );

    // Deleta agendamento
    void deleteByDataHoraAgendamentoAndCliente(
            LocalDateTime dataHoraAgendamento,
            String cliente
    );
}