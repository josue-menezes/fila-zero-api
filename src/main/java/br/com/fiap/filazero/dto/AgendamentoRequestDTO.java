package br.com.fiap.filazero.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Dados de entrada para um cidadão agendar um atendimento
 * (POST /cidadaos/{id}/agendamentos).
 */
public class AgendamentoRequestDTO {

    @NotNull(message = "A unidade é obrigatória")
    @Positive(message = "Código de unidade inválido")
    private Long cdUnidade;

    @NotNull(message = "O serviço é obrigatório")
    @Positive(message = "Código de serviço inválido")
    private Long cdServico;

    @NotNull(message = "A data e hora do agendamento são obrigatórias")
    @Future(message = "O agendamento deve ser para uma data e hora futuras")
    private LocalDateTime dtAgendamento;

    public Long getCdUnidade() { return cdUnidade; }
    public void setCdUnidade(Long cdUnidade) { this.cdUnidade = cdUnidade; }

    public Long getCdServico() { return cdServico; }
    public void setCdServico(Long cdServico) { this.cdServico = cdServico; }

    public LocalDateTime getDtAgendamento() { return dtAgendamento; }
    public void setDtAgendamento(LocalDateTime dtAgendamento) { this.dtAgendamento = dtAgendamento; }
}
