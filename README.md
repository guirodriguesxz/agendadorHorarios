# Agendador de Horários ⏰

[![CI](https://github.com/guirodriguesxz/agendadorHorarios/actions/workflows/ci.yml/badge.svg)](https://github.com/guirodriguesxz/agendadorHorarios/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)

API REST de agendamentos para barbearias, salões e pequenos negócios de serviço.
O foco do projeto é **garantir que dois clientes nunca fiquem com o mesmo horário do mesmo
profissional**, mesmo com requisições simultâneas, e **notificar novos agendamentos de forma
assíncrona** via RabbitMQ.

## Stack

- **Java 21 + Spring Boot 4** (Web MVC, Data JPA, Validation, AMQP)
- **PostgreSQL** + **Flyway** (schema versionado; Hibernate só valida)
- **RabbitMQ** — fila de notificação de novos agendamentos
- **JUnit 5 + MockMvc** — testes de integração dos endpoints
- **Docker / Docker Compose** e **GitHub Actions** (build + testes a cada push/PR)

## Decisões de projeto

### Como o conflito de horário é evitado

São três camadas, cada uma cobrindo um cenário:

| Camada | Onde | Cobre |
|---|---|---|
| Verificação na regra de negócio | `AgendamentoService.validarHorarioLivre` | Caso comum: retorna 409 com mensagem clara |
| `UNIQUE (profissional, data_hora_agendamento)` | Migration Flyway `V1` | **Condição de corrida**: duas requisições passam na verificação ao mesmo tempo; o banco aceita só a primeira e a segunda vira 409 |
| `@Version` (lock otimista) | Entidade `Agendamento` | Duas pessoas editando o **mesmo** agendamento ao mesmo tempo |

A verificação na aplicação sozinha não resolve concorrência. Quem garante a consistência é a constraint do banco.

### Por que RabbitMQ

Ao criar um agendamento, a API publica uma mensagem na fila durável `notificacao_agendamento`.
Quem envia a confirmação (e-mail, WhatsApp) consome essa fila **sem travar a resposta da API**.
Se o serviço de notificação cair, as mensagens ficam na fila e são processadas quando ele voltar.

## Como rodar

```bash
git clone https://github.com/guirodriguesxz/agendadorHorarios.git
cd agendadorHorarios

# Opção 1: tudo em containers (Postgres + RabbitMQ + API)
docker compose --profile app up --build

# Opção 2: só a infraestrutura em Docker e a API pela IDE / Maven
docker compose up -d
cd agendador-horarios && ./mvnw spring-boot:run
```

- API: http://localhost:8080
- Painel do RabbitMQ: http://localhost:15672 (admin / admin123)

## Endpoints

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| `POST` | `/agendamentos` | Cria agendamento | 201, 400 (validação), 409 (horário ocupado) |
| `GET` | `/agendamentos?data=2030-06-15` | Agenda do dia, ordenada por horário | 200 |
| `PUT` | `/agendamentos?cliente=...&dataHoraAgendamento=...` | Remarca / altera | 200, 404, 409 |
| `DELETE` | `/agendamentos?cliente=...&dataHoraAgendamento=...` | Cancela **somente** esse agendamento | 204, 404 |

### Exemplo

```bash
curl -X POST http://localhost:8080/agendamentos \
  -H "Content-Type: application/json" \
  -d '{
    "servico": "Corte + Barba",
    "profissional": "Carlos",
    "dataHoraAgendamento": "2030-06-15T14:30:00",
    "cliente": "Guilherme Rodrigues",
    "telefoneCliente": "11999999999"
  }'
```

Tentar o mesmo profissional e horário de novo:

```json
HTTP/1.1 409 Conflict
{
  "erro": "Conflito de Agendamento",
  "mensagem": "O profissional Carlos já possui agendamento em 2030-06-15T14:30"
}
```

Mais exemplos em [`teste.http`](teste.http), que dá para rodar direto no IntelliJ ou no VS Code (REST Client).

## Testes

```bash
cd agendador-horarios
./mvnw test
```

Os testes de integração sobem o contexto Spring com H2 em modo PostgreSQL, aplicando as
mesmas migrations do Flyway. O RabbitMQ é mockado. Cenários cobertos:

- criação e publicação da notificação na fila
- conflito do mesmo profissional no mesmo horário (409); profissionais diferentes no mesmo horário (ok)
- validação de payload e bloqueio de horário no passado
- agenda do dia retorna só aquele dia, em ordem
- cancelamento remove só o agendamento informado; inexistente retorna 404
- remarcação, inclusive para horário já ocupado (409)

## Próximos passos

- Consumidor da fila que envia a confirmação por WhatsApp/e-mail
- Duração do serviço (bloquear intervalos, não só o horário de início)
- Autenticação e Swagger/OpenAPI
- Testcontainers com Postgres real no CI

## Autor

Desenvolvido por Guilherme Rodrigues.
