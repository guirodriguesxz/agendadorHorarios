package com.guilhermerodrigues.agendador_horarios.controller;

import com.guilhermerodrigues.agendador_horarios.infrastructure.entity.Agendamento;
import com.guilhermerodrigues.agendador_horarios.services.AgendamentoService;
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
    public ResponseEntity<Agendamento> salvarAgendamento(@RequestBody Agendamento agendamento) {
        try {
            Agendamento salvo = agendamentoService.salvarAgendamento(agendamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Deletar agendamento por cliente e data/hora
    @DeleteMapping
    public ResponseEntity<Void> deletarAgendamento(
            @RequestParam String cliente,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraAgendamento) {
        try {
            agendamentoService.deletarAgendamento(dataHoraAgendamento, cliente);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar todos os agendamentos de um dia
    @GetMapping
    public ResponseEntity<List<Agendamento>> buscarAgendamentosDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        try {
            List<Agendamento> agendamentos = agendamentoService.buscarAgendamentosDia(data);
            return ResponseEntity.ok(agendamentos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Alterar agendamento
    @PutMapping
    public ResponseEntity<Agendamento> alterarAgendamentos(
            @RequestBody Agendamento agendamento,
            @RequestParam String cliente,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraAgendamento) {
        try {
            Agendamento atualizado = agendamentoService.alterarAgendamento(agendamento, cliente, dataHoraAgendamento);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}