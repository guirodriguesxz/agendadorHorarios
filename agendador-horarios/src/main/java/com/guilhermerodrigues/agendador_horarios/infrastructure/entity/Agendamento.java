package com.guilhermerodrigues.agendador_horarios.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agendamento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agendamento_profissional_horario",
                columnNames = {"profissional", "data_hora_agendamento"}))
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String servico;
    private String profissional;
    private LocalDateTime dataHoraAgendamento;
    private String cliente;
    private String telefoneCliente;
    private LocalDate dataInsercao = LocalDate.now();

    // Lock otimista: impede que duas alterações simultâneas no mesmo agendamento se sobrescrevam
    @Version
    private Long version;
}
