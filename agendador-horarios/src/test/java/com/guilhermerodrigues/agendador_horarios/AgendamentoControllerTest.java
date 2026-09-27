package com.guilhermerodrigues.agendador_horarios;

import com.guilhermerodrigues.agendador_horarios.config.infrastructure.RabbitMQConfig;
import com.guilhermerodrigues.agendador_horarios.infrastructure.repository.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository repository;

    // Os testes não dependem de um RabbitMQ rodando; só verificamos que a mensagem é enviada
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private final LocalDateTime amanha14h = LocalDateTime.now().plusDays(1)
            .withHour(14).truncatedTo(ChronoUnit.HOURS);

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private String json(String cliente, String profissional, LocalDateTime dataHora) {
        return """
                {"servico": "Corte", "profissional": "%s", "dataHoraAgendamento": "%s",
                 "cliente": "%s", "telefoneCliente": "11999999999"}
                """.formatted(profissional, dataHora, cliente);
    }

    private void criar(String cliente, String profissional, LocalDateTime dataHora) throws Exception {
        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(cliente, profissional, dataHora)))
                .andExpect(status().isCreated());
    }

    @Test
    void criaAgendamentoEPublicaNotificacao() throws Exception {
        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana", "Carlos", amanha14h)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cliente").value("Ana"));

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.FILA_NOTIFICACAO), anyString());
    }

    @Test
    void recusaMesmoProfissionalNoMesmoHorario() throws Exception {
        criar("Ana", "Carlos", amanha14h);

        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Bruno", "Carlos", amanha14h)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito de Agendamento"));
    }

    @Test
    void permiteProfissionaisDiferentesNoMesmoHorario() throws Exception {
        criar("Ana", "Carlos", amanha14h);
        criar("Bruno", "Diego", amanha14h);
    }

    @Test
    void validaPayload() throws Exception {
        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servico\": \"Corte\"}"))
                .andExpect(status().isBadRequest());

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void recusaHorarioNoPassado() throws Exception {
        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana", "Carlos", LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaSomenteAgendamentosDoDia() throws Exception {
        criar("Ana", "Carlos", amanha14h);
        criar("Bruno", "Carlos", amanha14h.plusHours(1));
        criar("Carla", "Carlos", amanha14h.plusDays(1));

        mockMvc.perform(get("/agendamentos").param("data", amanha14h.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cliente").value("Ana"))
                .andExpect(jsonPath("$[1].cliente").value("Bruno"));
    }

    @Test
    void deletaSomenteOAgendamentoInformado() throws Exception {
        criar("Ana", "Carlos", amanha14h);
        criar("Bruno", "Carlos", amanha14h.plusHours(1));

        mockMvc.perform(delete("/agendamentos")
                        .param("cliente", "Ana")
                        .param("dataHoraAgendamento", amanha14h.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/agendamentos").param("data", amanha14h.toLocalDate().toString()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cliente").value("Bruno"));
    }

    @Test
    void deletarInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/agendamentos")
                        .param("cliente", "Ninguem")
                        .param("dataHoraAgendamento", amanha14h.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void remarcaAgendamento() throws Exception {
        criar("Ana", "Carlos", amanha14h);
        LocalDateTime novoHorario = amanha14h.plusHours(2);

        mockMvc.perform(put("/agendamentos")
                        .param("cliente", "Ana")
                        .param("dataHoraAgendamento", amanha14h.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana", "Carlos", novoHorario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataHoraAgendamento").value(novoHorario.toString() + ":00"));
    }

    @Test
    void remarcarParaHorarioOcupadoRetorna409() throws Exception {
        criar("Ana", "Carlos", amanha14h);
        criar("Bruno", "Carlos", amanha14h.plusHours(1));

        mockMvc.perform(put("/agendamentos")
                        .param("cliente", "Ana")
                        .param("dataHoraAgendamento", amanha14h.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana", "Carlos", amanha14h.plusHours(1))))
                .andExpect(status().isConflict());
    }
}
