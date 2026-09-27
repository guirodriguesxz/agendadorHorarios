package com.guilhermerodrigues.agendador_horarios.config.infrastructure;

import com.guilhermerodrigues.agendador_horarios.services.exceptions.AgendamentoNaoEncontradoException;
import com.guilhermerodrigues.agendador_horarios.services.exceptions.HorarioIndisponivelException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AgendamentoExceptionHandler {

    private static final String MSG_CONFLITO =
            "Poxa, outra pessoa acabou de reservar este mesmo horário na sua frente. Por favor, escolha outro horário.";

    @ExceptionHandler(HorarioIndisponivelException.class)
    public ResponseEntity<Map<String, String>> handleHorarioIndisponivel(HorarioIndisponivelException ex) {
        return erro(HttpStatus.CONFLICT, "Conflito de Agendamento", ex.getMessage());
    }

    // Corrida entre duas requisições para o mesmo horário: a UNIQUE constraint do banco barra a segunda
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegridade(DataIntegrityViolationException ex) {
        return erro(HttpStatus.CONFLICT, "Conflito de Agendamento", MSG_CONFLITO);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleLockOtimista(ObjectOptimisticLockingFailureException ex) {
        return erro(HttpStatus.CONFLICT, "Conflito de Agendamento", MSG_CONFLITO);
    }

    @ExceptionHandler(AgendamentoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(AgendamentoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, "Agendamento não encontrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException ex) {
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", erro, "mensagem", mensagem));
    }
}
