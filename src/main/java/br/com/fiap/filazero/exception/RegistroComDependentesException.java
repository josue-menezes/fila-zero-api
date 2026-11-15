package br.com.fiap.filazero.exception;

/**
 * RN07: não é possível remover um registro que possui agendamentos ou outros vínculos.
 */
public class RegistroComDependentesException extends RegraDeNegocioException {

    public RegistroComDependentesException(String mensagem) {
        super(mensagem);
    }
}
