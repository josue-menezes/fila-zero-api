package br.com.fiap.filazero.exception;

/**
 * RN02: a unidade não oferece o serviço escolhido.
 */
public class ServicoIndisponivelException extends RegraDeNegocioException {

    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
