package br.com.fiap.filazero.exception;

/**
 * RN05: cancelamento só é permitido até 2 horas antes do horário marcado.
 */
public class CancelamentoForaDoPrazoException extends RegraDeNegocioException {

    public CancelamentoForaDoPrazoException(String mensagem) {
        super(mensagem);
    }
}
