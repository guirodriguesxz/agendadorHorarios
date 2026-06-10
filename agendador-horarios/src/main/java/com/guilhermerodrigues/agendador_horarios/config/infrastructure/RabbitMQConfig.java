package com.guilhermerodrigues.agendador_horarios.config.infrastructure;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_NOTIFICACAO = "notificacao_agendamento";

    @Bean
    public Queue queue() {
        // Cria uma fila durável (não se perde se o RabbitMQ reiniciar)
        return new Queue(FILA_NOTIFICACAO, true);
    }
}
