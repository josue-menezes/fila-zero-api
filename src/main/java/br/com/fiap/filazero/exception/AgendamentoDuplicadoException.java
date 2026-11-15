package br.com.fiap.filazero.exception;

/**
 * RN04: o cidadão já tem um agendamento ativo para o mesmo serviço.
 */
public class AgendamentoDuplicadoException extends RegraDeNegocioException {

    public AgendamentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
