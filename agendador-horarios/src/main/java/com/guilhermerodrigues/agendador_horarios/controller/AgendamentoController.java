package com.guilhermerodrigues.agendador_horarios.controller;

import com.guilhermerodrigues.agendador_horarios.controller.dto.AgendamentoRequest;
import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import com.guilhermerodrigues.agendador_horarios.services.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    // Criação de agendamento
    @PostMapping
    public ResponseEntity<Agendamento> salvarAgendamento(@RequestBody @Valid AgendamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.salvarAgendamento(request));
    }

    // Deletar agendamento por cliente e data/hora
    @DeleteMapping
    public ResponseEntity<Void> deletarAgendamento(
            @RequestParam String cliente,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraAgendamento) {
        agendamentoService.deletarAgendamento(dataHoraAgendamento, cliente);
        return ResponseEntity.noContent().build();
    }

    // Buscar todos os agendamentos de um dia
    @GetMapping
    public ResponseEntity<List<Agendamento>> buscarAgendamentosDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentosDia(data));
    }

    // Alterar agendamento
    @PutMapping
    public ResponseEntity<Agendamento> alterarAgendamento(
            @RequestBody @Valid AgendamentoRequest request,
            @RequestParam String cliente,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraAgendamento) {
        return ResponseEntity.ok(agendamentoService.alterarAgendamento(request, cliente, dataHoraAgendamento));
    }
}
