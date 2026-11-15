package br.com.fiap.filazero.exception;

/**
 * RN01: o horário já atingiu o número de guichês da unidade.
 */
public class HorarioLotadoException extends RegraDeNegocioException {

    public HorarioLotadoException(String mensagem) {
        super(mensagem);
    }
}
