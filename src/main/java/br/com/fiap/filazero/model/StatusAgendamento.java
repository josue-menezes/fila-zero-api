package br.com.fiap.filazero.model;

/**
 * Situações possíveis de um agendamento (coluna st_agendamento).
 */
public enum StatusAgendamento {
    AGENDADO,
    CONFIRMADO,
    CANCELADO,
    ATENDIDO,
    FALTOU;

    /** Agendamento ativo: ainda ocupa vaga no horário (RN01). */
    public boolean isAtivo() {
        return this == AGENDADO || this == CONFIRMADO;
    }

    /** Status final: não pode mais ser alterado (RN06). */
    public boolean isFinal() {
        return this == CANCELADO || this == ATENDIDO || this == FALTOU;
    }
}
