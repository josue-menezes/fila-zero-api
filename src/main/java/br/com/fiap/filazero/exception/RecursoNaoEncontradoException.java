package br.com.fiap.filazero.exception;

/**
 * Lançada quando um registro buscado pelo código não existe (HTTP 404).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
