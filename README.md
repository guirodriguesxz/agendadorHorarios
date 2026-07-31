# Agendador de Horários ⏰

API REST para gerenciamento de agendamentos de horários, ideal para barbearias, salões de beleza e pequenos negócios de serviço. Desenvolvida em Java com Spring Boot, permite criar, consultar, atualizar e cancelar agendamentos de forma simples e organizada.

## Tecnologias utilizadas

- Java 21
- Spring Boot (Web, Data JPA)
- H2 Database (banco em memória para desenvolvimento)
- RabbitMQ (mensageria, utilizada no controle de concorrência de agendamentos)
- Docker e Docker Compose
- Lombok

## Funcionalidades

- Criar agendamento: POST /agendamentos
- Consultar agendamentos de um dia especifico: GET /agendamentos
- Atualizar um agendamento existente: PUT /agendamentos
- Cancelar um agendamento: DELETE /agendamentos

## Como rodar o projeto localmente

1. Clone o repositorio
2. Suba a infraestrutura do RabbitMQ com Docker
3. Entre na pasta agendador-horarios
4. Execute a aplicacao com o Maven Wrapper
5. A API estara disponivel em http://localhost:8080

Comandos:

```
git clone https://github.com/guirodriguesxz/agendadorHorarios.git
cd agendadorHorarios
docker-compose up -d
cd agendador-horarios
./mvnw spring-boot:run
```

## Exemplo de requisicao

```
POST http://localhost:8080/agendamentos
Content-Type: application/json

{
  "cliente": "Nome do Cliente",
  "dataHoraAgendamento": "2026-06-15T14:30:00"
}
```

## Autor

Desenvolvido por Guilherme Rodrigues.
