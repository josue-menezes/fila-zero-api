package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDeDadosException;
import br.com.fiap.filazero.model.Servico;
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
 * Acesso às tabelas tb_servico e tb_unidade_servico (ofertas) com JDBC puro.
 */
@Repository
public class ServicoDAO {

    private static final String COLUNAS = "cd_servico, nm_servico, ds_servico, ds_documentos, nr_duracao_min";

    private final DataSource dataSource;

    public ServicoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Servico inserir(Servico servico) {
        String sql = "INSERT INTO tb_servico (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            Long novoCodigo = SequenceUtil.proximoValor(conexao, "sq_tb_servico");
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, novoCodigo);
                preencherCampos(ps, servico, 2);
                ps.executeUpdate();
            }
            servico.setCdServico(novoCodigo);
            return servico;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar serviço", e);
        }
    }

    public List<Servico> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_servico ORDER BY nm_servico";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Servico> servicos = new ArrayList<>();
            while (rs.next()) {
                servicos.add(montar(rs));
            }
            return servicos;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar serviços", e);
        }
    }

    public Optional<Servico> buscarPorId(Long cdServico) {
        String sql = "SELECT " + COLUNAS + " FROM tb_servico WHERE cd_servico = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdServico);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar serviço", e);
        }
    }

    public void atualizar(Servico servico) {
        String sql = "UPDATE tb_servico SET nm_servico = ?, ds_servico = ?, ds_documentos = ?, nr_duracao_min = ? "
                + "WHERE cd_servico = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencherCampos(ps, servico, 1);
            ps.setLong(5, servico.getCdServico());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar serviço", e);
        }
    }

    public void remover(Long cdServico) {
        String sql = "DELETE FROM tb_servico WHERE cd_servico = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdServico);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao remover serviço", e);
        }
    }

    public boolean existeNome(String nmServico, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_servico WHERE UPPER(nm_servico) = UPPER(?) AND cd_servico <> ?";
        return SequenceUtil.contar(dataSource, sql, nmServico, ignorarCodigo == null ? -1L : ignorarCodigo) > 0;
    }

    /** Conta agendamentos e ofertas ligados ao serviço (RN07). */
    public int contarDependentes(Long cdServico) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_agendamento WHERE cd_servico = ?) "
                + "+ (SELECT COUNT(*) FROM tb_unidade_servico WHERE cd_servico = ?) FROM dual";
        return SequenceUtil.contar(dataSource, sql, cdServico, cdServico);
    }

    /** RN02: a unidade oferece este serviço com a oferta ativa? */
    public boolean unidadeOferece(Long cdUnidade, Long cdServico) {
        String sql = "SELECT COUNT(*) FROM tb_unidade_servico "
                + "WHERE cd_unidade = ? AND cd_servico = ? AND fl_ativo = 'S'";
        return SequenceUtil.contar(dataSource, sql, cdUnidade, cdServico) > 0;
    }

    private void preencherCampos(PreparedStatement ps, Servico servico, int inicio) throws SQLException {
        ps.setString(inicio, servico.getNmServico());
        ps.setString(inicio + 1, servico.getDsServico());
        ps.setString(inicio + 2, servico.getDsDocumentos());
        ps.setInt(inicio + 3, servico.getNrDuracaoMin());
    }

    private Servico montar(ResultSet rs) throws SQLException {
        Servico servico = new Servico();
        servico.setCdServico(rs.getLong("cd_servico"));
        servico.setNmServico(rs.getString("nm_servico"));
        servico.setDsServico(rs.getString("ds_servico"));
        servico.setDsDocumentos(rs.getString("ds_documentos"));
        servico.setNrDuracaoMin(rs.getInt("nr_duracao_min"));
        return servico;
    }
}
