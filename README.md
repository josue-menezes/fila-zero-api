# Fila Zero API

API RESTful do **Fila Zero**, solução da Global Solution 2026/2 (tema CitizenTech) da turma 1TDSR da FIAP.

O Fila Zero é o "Waze das filas públicas": mostra a espera prevista em cada unidade de atendimento (estilo Poupatempo), deixa o cidadão agendar o melhor horário e reduz faltas com lembretes.

## Integrantes

| Nome | RM | Turma |
| --- | --- | --- |
| Arthur Cordão | 570313 | 1TDSR |
| Guilherme Novaes | 570444 | 1TDSR |
| Jeferson Faria | 573038 | 1TDSR |
| Josué Menezes | 569220 | 1TDSR |

## Links

- **API no Render:** https://COLE-AQUI-A-URL.onrender.com
- **Vídeo (YouTube, não listado):** COLE-AQUI-O-LINK

## Tecnologias

- Java 21, Spring Boot 3.4, Maven
- Oracle (banco da FIAP) com **JDBC puro** (DAO + PreparedStatement), sem JPA/Hibernate/Spring Data
- Bean Validation, ModelMapper, Docker e Render

## Estrutura dos pacotes

| Pacote | Responsabilidade |
| --- | --- |
| `model` | Entidades do domínio: Unidade, Servico, Cidadao, Agendamento e StatusAgendamento |
| `dao` | Acesso ao Oracle com JDBC (um DAO por entidade, DataSource pelo construtor) |
| `dto` | Entrada (com validações) e saída da API; a API nunca recebe nem devolve o model |
| `service` | Regras de negócio (RN01 a RN07) e conversão model ⇄ DTO com ModelMapper |
| `controller` | Rotas REST e status HTTP |
| `exception` | Exceções próprias do domínio |
| `handler` | `@RestControllerAdvice` que devolve todo erro num JSON padronizado |
| `config` | CORS e ModelMapper |

## Como rodar localmente

1. Rode o script `sql/fila_zero_banco.sql` no seu usuário do Oracle da FIAP (só na primeira vez).
2. Abra o projeto no IntelliJ e configure as variáveis de ambiente na configuração de execução:
   - `DB_URL` = `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
   - `DB_USER` = seu RM (ex.: `RM569220`)
   - `DB_PASSWORD` = sua senha do Oracle
3. Rode a classe `FilaZeroApplication`. A API sobe em `http://localhost:8080`.
4. Importe `insomnia/fila-zero-insomnia.json` no Insomnia para testar todas as rotas.

As credenciais **nunca** ficam no código: são lidas só das variáveis de ambiente.

## Rotas

| Método | Rota | Sucesso | Erros |
| --- | --- | --- | --- |
| GET | /unidades | 200 | — |
| GET | /unidades/{id} | 200 | 404 |
| POST | /unidades | 201 + Location | 400, 409 |
| PUT | /unidades/{id} | 200 | 400, 404, 409 |
| DELETE | /unidades/{id} | 204 | 404, 409 |
| GET | /unidades/{id}/agendamentos?data=AAAA-MM-DD | 200 | 404 |
| GET | /unidades/{id}/horarios?data=AAAA-MM-DD | 200 | 400, 404 |
| GET | /servicos | 200 | — |
| GET | /servicos/{id} | 200 | 404 |
| POST | /servicos | 201 + Location | 400, 409 |
| PUT | /servicos/{id} | 200 | 400, 404, 409 |
| DELETE | /servicos/{id} | 204 | 404, 409 |
| GET | /servicos/{id}/unidades | 200 | 404 |
| GET | /cidadaos | 200 | — |
| GET | /cidadaos/{id} | 200 | 404 |
| POST | /cidadaos | 201 + Location | 400, 409 |
| PUT | /cidadaos/{id} | 200 | 400, 404, 409 |
| DELETE | /cidadaos/{id} | 204 | 404, 409 |
| GET | /cidadaos/{id}/agendamentos | 200 | 404 |
| POST | /cidadaos/{id}/agendamentos | 201 + Location | 400, 404, 409 |
| GET | /agendamentos | 200 | — |
| GET | /agendamentos/{id} | 200 | 404 |
| PUT | /agendamentos/{id} | 200 | 400, 404, 409 |
| DELETE | /agendamentos/{id} | 204 | 404 |

Método não suportado: 405. Erro inesperado: 500 com mensagem genérica.

## Regras de negócio

| ID | Regra | Exceção (409) |
| --- | --- | --- |
| RN01 | Um horário aceita no máximo `nr_guiches` agendamentos ativos | `HorarioLotadoException` |
| RN02 | A unidade precisa oferecer o serviço | `ServicoIndisponivelException` |
| RN03 | Horário cheio ou :30, dentro do funcionamento da unidade | `HorarioInvalidoException` |
| RN04 | Um agendamento ativo por serviço para cada cidadão | `AgendamentoDuplicadoException` |
| RN05 | Cancelamento só até 2 horas antes | `CancelamentoForaDoPrazoException` |
| RN06 | Atendido, falta ou cancelado não pode mais ser alterado | `AgendamentoFinalizadoException` |
| RN07 | Não remove registro com agendamentos ou vínculos | `RegistroComDependentesException` |

## Deploy (Render)

Dockerfile multi-stage na raiz. No Render: **New → Web Service → Docker**, apontando para este repositório, com as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`.
