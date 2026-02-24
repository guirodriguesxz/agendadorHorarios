package com.guilhermerodrigues.agendador_horarios.services;

import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import com.guilhermerodrigues.agendador_horarios.infrastructure.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    // ===============================
    // SALVAR
    // ===============================
    public Agendamento salvarAgendamento(Agendamento agendamento) {

        LocalDateTime horarioInicio = agendamento.getDataHoraAgendamento();
        LocalDateTime horarioFim = horarioInicio.plusHours(1);

        Agendamento conflito =
                agendamentoRepository.findByServicoAndDataHoraAgendamentoBetween(
                        agendamento.getServico(),
                        horarioInicio,
                        horarioFim
                );

        if (Objects.nonNull(conflito)) {
            throw new RuntimeException("Horário já está preenchido");
        }

        return agendamentoRepository.save(agendamento);
    }

    // ===============================
    // BUSCAR TODOS DO DIA (LISTA)
    // ===============================
    public List<Agendamento> buscarAgendamentosDia(LocalDate data) {

        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);

        return agendamentoRepository
                .findByDataHoraAgendamentoBetween(inicioDia, fimDia);
    }

    // ===============================
    // DELETAR
    // ===============================
    public void deletarAgendamento(LocalDateTime dataHoraAgendamento, String cliente) {

        Agendamento agendamento =
                agendamentoRepository.findByDataHoraAgendamentoAndCliente(
                        dataHoraAgendamento,
                        cliente
                );

        if (Objects.isNull(agendamento)) {
            throw new RuntimeException("Agendamento não encontrado");
        }

        agendamentoRepository.deleteByDataHoraAgendamentoAndCliente(
                dataHoraAgendamento,
                cliente
        );
    }

    // ===============================
    // ALTERAR
    // ===============================
    public Agendamento alterarAgendamento(
            Agendamento novoAgendamento,
            String cliente,
            LocalDateTime dataHoraAgendamento
    ) {

        Agendamento existente =
                agendamentoRepository.findByDataHoraAgendamentoAndCliente(
                        dataHoraAgendamento,
                        cliente
                );

        if (Objects.isNull(existente)) {
            throw new RuntimeException("Agendamento não encontrado");
        }

        novoAgendamento.setId(existente.getId());

        return agendamentoRepository.save(novoAgendamento);
    }
}