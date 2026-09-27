package com.guilhermerodrigues.agendador_horarios.infrastructure.repository;

import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Verifica se o profissional já tem alguém naquele horário
    boolean existsByProfissionalAndDataHoraAgendamento(String profissional, LocalDateTime dataHoraAgendamento);

    // Busca todos os agendamentos do dia, em ordem de horário
    List<Agendamento> findByDataHoraAgendamentoBetweenOrderByDataHoraAgendamento(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca agendamento específico
    Optional<Agendamento> findByDataHoraAgendamentoAndCliente(
            LocalDateTime dataHoraAgendamento,
            String cliente
    );
}
