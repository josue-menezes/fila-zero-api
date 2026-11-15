package br.com.fiap.filazero.exception;

/**
 * Falha ao acessar o banco Oracle. O handler devolve 500 com mensagem genérica,
 * sem expor o SQL nem o erro original ao cliente.
 */
public class BancoDeDadosException extends RuntimeException {

    public BancoDeDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
