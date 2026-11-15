package br.com.fiap.filazero.exception;

/**
 * Base de todas as violações de regra de negócio do Fila Zero (HTTP 409).
 * Cada regra tem sua própria exceção, que estende esta.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
