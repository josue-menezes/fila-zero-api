package br.com.fiap.filazero.exception;

/**
 * Dados de entrada que passam no Bean Validation, mas são incoerentes entre si
 * (ex.: hora de fechamento antes da abertura). HTTP 400.
 */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
