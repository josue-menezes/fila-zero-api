package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.StatusAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Dados de entrada para confirmar, cancelar ou reagendar (PUT /agendamentos/{id}).
 * A data só precisa vir quando for reagendamento.
 */
public class AgendamentoUpdateDTO {

    @NotNull(message = "O status é obrigatório (AGENDADO, CONFIRMADO, CANCELADO, ATENDIDO ou FALTOU)")
    private StatusAgendamento status;

    @Future(message = "O novo horário deve ser uma data e hora futuras")
    private LocalDateTime dtAgendamento;

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public LocalDateTime getDtAgendamento() { return dtAgendamento; }
    public void setDtAgendamento(LocalDateTime dtAgendamento) { this.dtAgendamento = dtAgendamento; }
}
