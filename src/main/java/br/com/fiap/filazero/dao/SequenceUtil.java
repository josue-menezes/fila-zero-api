package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDeDadosException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Funções de apoio usadas por todos os DAOs:
 * pegar o próximo valor de uma sequence e executar consultas de contagem.
 */
final class SequenceUtil {

    private SequenceUtil() {
    }

    /** Pega o próximo código da sequence (ex.: sq_tb_unidade.NEXTVAL). */
    static Long proximoValor(Connection conexao, String sequence) throws SQLException {
        String sql = "SELECT " + sequence + ".NEXTVAL FROM dual";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Executa um SELECT COUNT(*) com parâmetros e devolve o número. */
    static int contar(DataSource dataSource, String sql, Object... parametros) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao consultar o banco", e);
        }
    }
}
