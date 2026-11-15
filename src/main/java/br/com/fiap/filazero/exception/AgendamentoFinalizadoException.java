package br.com.fiap.filazero.exception;

/**
 * RN06: agendamento atendido, com falta ou cancelado não pode mais ser alterado.
 */
public class AgendamentoFinalizadoException extends RegraDeNegocioException {

    public AgendamentoFinalizadoException(String mensagem) {
        super(mensagem);
    }
}
