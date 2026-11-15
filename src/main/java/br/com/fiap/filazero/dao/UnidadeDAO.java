package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDeDadosException;
import br.com.fiap.filazero.model.Unidade;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_unidade com JDBC puro (PreparedStatement).
 * O DataSource chega pelo construtor (injeção de dependência do Spring).
 */
@Repository
public class UnidadeDAO {

    private static final String COLUNAS =
            "cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_guiches, hr_abertura, hr_fechamento";

    private final DataSource dataSource;

    public UnidadeDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Unidade inserir(Unidade unidade) {
        String sql = "INSERT INTO tb_unidade (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            Long novoCodigo = SequenceUtil.proximoValor(conexao, "sq_tb_unidade");
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, novoCodigo);
                preencherCampos(ps, unidade, 2);
                ps.executeUpdate();
            }
            unidade.setCdUnidade(novoCodigo);
            return unidade;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar unidade", e);
        }
    }

    public List<Unidade> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_unidade ORDER BY nm_unidade";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Unidade> unidades = new ArrayList<>();
            while (rs.next()) {
                unidades.add(montar(rs));
            }
            return unidades;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar unidades", e);
        }
    }

    public Optional<Unidade> buscarPorId(Long cdUnidade) {
        String sql = "SELECT " + COLUNAS + " FROM tb_unidade WHERE cd_unidade = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdUnidade);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar unidade", e);
        }
    }

    /** Unidades com oferta ativa de um serviço (relacionamento N:N via tb_unidade_servico). */
    public List<Unidade> listarPorServico(Long cdServico) {
        String sql = "SELECT u.cd_unidade, u.nm_unidade, u.ds_endereco, u.nm_bairro, u.nm_cidade, "
                + "u.nr_guiches, u.hr_abertura, u.hr_fechamento "
                + "FROM tb_unidade u "
                + "INNER JOIN tb_unidade_servico us ON us.cd_unidade = u.cd_unidade "
                + "WHERE us.cd_servico = ? AND us.fl_ativo = 'S' "
                + "ORDER BY u.nm_unidade";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdServico);
            try (ResultSet rs = ps.executeQuery()) {
                List<Unidade> unidades = new ArrayList<>();
                while (rs.next()) {
                    unidades.add(montar(rs));
                }
                return unidades;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar unidades do serviço", e);
        }
    }

    public void atualizar(Unidade unidade) {
        String sql = "UPDATE tb_unidade SET nm_unidade = ?, ds_endereco = ?, nm_bairro = ?, nm_cidade = ?, "
                + "nr_guiches = ?, hr_abertura = ?, hr_fechamento = ? WHERE cd_unidade = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencherCampos(ps, unidade, 1);
            ps.setLong(8, unidade.getCdUnidade());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar unidade", e);
        }
    }

    public void remover(Long cdUnidade) {
        String sql = "DELETE FROM tb_unidade WHERE cd_unidade = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdUnidade);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao remover unidade", e);
        }
    }

    /** Verifica se já existe outra unidade com o mesmo nome (nm_unidade é UNIQUE). */
    public boolean existeNome(String nmUnidade, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_unidade WHERE UPPER(nm_unidade) = UPPER(?) AND cd_unidade <> ?";
        return SequenceUtil.contar(dataSource, sql, nmUnidade, ignorarCodigo == null ? -1L : ignorarCodigo) > 0;
    }

    /** Conta agendamentos, atendentes e ofertas ligados à unidade (RN07). */
    public int contarDependentes(Long cdUnidade) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_agendamento WHERE cd_unidade = ?) "
                + "+ (SELECT COUNT(*) FROM tb_atendente WHERE cd_unidade = ?) "
                + "+ (SELECT COUNT(*) FROM tb_unidade_servico WHERE cd_unidade = ?) FROM dual";
        return SequenceUtil.contar(dataSource, sql, cdUnidade, cdUnidade, cdUnidade);
    }

    private void preencherCampos(PreparedStatement ps, Unidade unidade, int inicio) throws SQLException {
        ps.setString(inicio, unidade.getNmUnidade());
        ps.setString(inicio + 1, unidade.getDsEndereco());
        ps.setString(inicio + 2, unidade.getNmBairro());
        ps.setString(inicio + 3, unidade.getNmCidade());
        ps.setInt(inicio + 4, unidade.getGuiches());
        ps.setInt(inicio + 5, unidade.getHrAbertura());
        ps.setInt(inicio + 6, unidade.getHrFechamento());
    }

    private Unidade montar(ResultSet rs) throws SQLException {
        Unidade unidade = new Unidade();
        unidade.setCdUnidade(rs.getLong("cd_unidade"));
        unidade.setNmUnidade(rs.getString("nm_unidade"));
        unidade.setDsEndereco(rs.getString("ds_endereco"));
        unidade.setNmBairro(rs.getString("nm_bairro"));
        unidade.setNmCidade(rs.getString("nm_cidade"));
        unidade.setGuiches(rs.getInt("nr_guiches"));
        unidade.setHrAbertura(rs.getInt("hr_abertura"));
        unidade.setHrFechamento(rs.getInt("hr_fechamento"));
        return unidade;
    }
}
