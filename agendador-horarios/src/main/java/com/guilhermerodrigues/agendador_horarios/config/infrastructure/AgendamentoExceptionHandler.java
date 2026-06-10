package com.guilhermerodrigues.agendador_horarios.config.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class AgendamentoExceptionHandler{

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleException(ObjectOptimisticLockingFailureException ex) {
        Map<String, String> respostaErro = new HashMap<>();
        respostaErro.put("erro","Conflito de Agendamento");
        respostaErro.put("mensagem","Poxa, Outra pessoa acabou de reservar este mesmo horário na sua frente. Por favor, escolha outro horário.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respostaErro);
    }

}
