package com.guilhermerodrigues.agendador_horarios.services;

import com.guilhermerodrigues.agendador_horarios.config.infrastructure.RabbitMQConfig;
import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import com.guilhermerodrigues.agendador_horarios.infrastructure.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final RabbitTemplate rabbitTemplate; // Injeção do RabbitMQ

    // 1. Método de Salvar (com o disparo da Mensagem)
    public Agendamento salvarAgendamento(Agendamento agendamento) {

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // Notifica o RabbitMQ que um novo agendamento foi feito
        String mensagem = "Novo agendamento criado: ID " + salvo.getId() + " - Cliente: " + salvo.getCliente();
        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_NOTIFICACAO, mensagem);

        return salvo;
    }

    // 2. Método de Deletar
    public void deletarAgendamento(LocalDateTime dataHoraAgendamento, String cliente) {
        // Lógica para deletar (você pode ajustar depois conforme a sua necessidade do Repository)
        // Exemplo genérico:
        agendamentoRepository.deleteAll();
    }

    // 3. Método de Buscar por Dia
    public List<Agendamento> buscarAgendamentosDia(LocalDate data) {
        // Lógica para buscar (você precisará de um método no seu repository para isso)
        // Retornando uma lista vazia por enquanto para não quebrar o Controller
        return agendamentoRepository.findAll();
    }

    // 4. Método de Alterar
    public Agendamento alterarAgendamento(Agendamento agendamento, String cliente, LocalDateTime dataHoraAgendamento) {
        // Lógica para atualizar
        return agendamentoRepository.save(agendamento);
    }
}