package com.guilhermerodrigues.agendador_horarios.services;

import com.guilhermerodrigues.agendador_horarios.config.infrastructure.RabbitMQConfig;
import com.guilhermerodrigues.agendador_horarios.controller.dto.AgendamentoRequest;
import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import com.guilhermerodrigues.agendador_horarios.infrastructure.repository.AgendamentoRepository;
import com.guilhermerodrigues.agendador_horarios.services.exceptions.AgendamentoNaoEncontradoException;
import com.guilhermerodrigues.agendador_horarios.services.exceptions.HorarioIndisponivelException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Agendamento salvarAgendamento(AgendamentoRequest request) {
        validarHorarioLivre(request.profissional(), request.dataHoraAgendamento());

        Agendamento agendamento = new Agendamento();
        aplicar(request, agendamento);
        // saveAndFlush força o INSERT agora: se outra requisição pegou o horário entre a
        // verificação acima e este ponto, a UNIQUE constraint do banco barra aqui (409)
        Agendamento salvo = agendamentoRepository.saveAndFlush(agendamento);

        // Notificação assíncrona: quem consome a fila (e-mail, WhatsApp...) não trava a API
        String mensagem = "Novo agendamento criado: ID " + salvo.getId() + " - Cliente: " + salvo.getCliente();
        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_NOTIFICACAO, mensagem);

        return salvo;
    }

    @Transactional
    public void deletarAgendamento(LocalDateTime dataHoraAgendamento, String cliente) {
        agendamentoRepository.delete(buscar(dataHoraAgendamento, cliente));
    }

    @Transactional(readOnly = true)
    public List<Agendamento> buscarAgendamentosDia(LocalDate data) {
        return agendamentoRepository.findByDataHoraAgendamentoBetweenOrderByDataHoraAgendamento(
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    @Transactional
    public Agendamento alterarAgendamento(AgendamentoRequest request, String cliente, LocalDateTime dataHoraAgendamento) {
        Agendamento existente = buscar(dataHoraAgendamento, cliente);

        boolean mudouHorario = !Objects.equals(existente.getProfissional(), request.profissional())
                || !Objects.equals(existente.getDataHoraAgendamento(), request.dataHoraAgendamento());
        if (mudouHorario) {
            validarHorarioLivre(request.profissional(), request.dataHoraAgendamento());
        }

        aplicar(request, existente);
        return agendamentoRepository.saveAndFlush(existente);
    }

    private Agendamento buscar(LocalDateTime dataHoraAgendamento, String cliente) {
        return agendamentoRepository.findByDataHoraAgendamentoAndCliente(dataHoraAgendamento, cliente)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException(cliente, dataHoraAgendamento));
    }

    private void validarHorarioLivre(String profissional, LocalDateTime dataHora) {
        if (agendamentoRepository.existsByProfissionalAndDataHoraAgendamento(profissional, dataHora)) {
            throw new HorarioIndisponivelException(profissional, dataHora);
        }
    }

    private void aplicar(AgendamentoRequest request, Agendamento agendamento) {
        agendamento.setServico(request.servico());
        agendamento.setProfissional(request.profissional());
        agendamento.setDataHoraAgendamento(request.dataHoraAgendamento());
        agendamento.setCliente(request.cliente());
        agendamento.setTelefoneCliente(request.telefoneCliente());
    }
}
