package br.com.fiap.filazero.exception;

/**
 * Já existe um registro com o mesmo valor único (ex.: CPF, e-mail ou nome).
 */
public class RegistroDuplicadoException extends RegraDeNegocioException {

    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
