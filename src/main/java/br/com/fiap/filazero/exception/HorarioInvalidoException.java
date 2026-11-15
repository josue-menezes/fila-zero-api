package br.com.fiap.filazero.exception;

/**
 * RN03: horário fora do funcionamento da unidade ou fora dos intervalos de 30 minutos.
 */
public class HorarioInvalidoException extends RegraDeNegocioException {

    public HorarioInvalidoException(String mensagem) {
        super(mensagem);
    }
}
