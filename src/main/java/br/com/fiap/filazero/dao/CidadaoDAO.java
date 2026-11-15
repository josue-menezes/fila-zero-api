package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDeDadosException;
import br.com.fiap.filazero.model.Cidadao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_cidadao com JDBC puro.
 */
@Repository
public class CidadaoDAO {

    private static final String COLUNAS =
            "cd_cidadao, nm_cidadao, nr_cpf, ds_email, nr_telefone, dt_nascimento, nm_bairro";

    private final DataSource dataSource;

    public CidadaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Cidadao inserir(Cidadao cidadao) {
        String sql = "INSERT INTO tb_cidadao (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            Long novoCodigo = SequenceUtil.proximoValor(conexao, "sq_tb_cidadao");
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, novoCodigo);
                preencherCampos(ps, cidadao, 2);
                ps.executeUpdate();
            }
            cidadao.setCdCidadao(novoCodigo);
            return cidadao;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar cidadão", e);
        }
    }

    public List<Cidadao> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_cidadao ORDER BY nm_cidadao";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Cidadao> cidadaos = new ArrayList<>();
            while (rs.next()) {
                cidadaos.add(montar(rs));
            }
            return cidadaos;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar cidadãos", e);
        }
    }

    public Optional<Cidadao> buscarPorId(Long cdCidadao) {
        String sql = "SELECT " + COLUNAS + " FROM tb_cidadao WHERE cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdCidadao);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar cidadão", e);
        }
    }

    public void atualizar(Cidadao cidadao) {
        String sql = "UPDATE tb_cidadao SET nm_cidadao = ?, nr_cpf = ?, ds_email = ?, nr_telefone = ?, "
                + "dt_nascimento = ?, nm_bairro = ? WHERE cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencherCampos(ps, cidadao, 1);
            ps.setLong(7, cidadao.getCdCidadao());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar cidadão", e);
        }
    }

    public void remover(Long cdCidadao) {
        String sql = "DELETE FROM tb_cidadao WHERE cd_cidadao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdCidadao);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao remover cidadão", e);
        }
    }

    public boolean existeCpf(String nrCpf, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_cidadao WHERE nr_cpf = ? AND cd_cidadao <> ?";
        return SequenceUtil.contar(dataSource, sql, nrCpf, ignorarCodigo == null ? -1L : ignorarCodigo) > 0;
    }

    public boolean existeEmail(String dsEmail, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_cidadao WHERE LOWER(ds_email) = LOWER(?) AND cd_cidadao <> ?";
        return SequenceUtil.contar(dataSource, sql, dsEmail, ignorarCodigo == null ? -1L : ignorarCodigo) > 0;
    }

    /** RN07: quantos agendamentos o cidadão tem. */
    public int contarAgendamentos(Long cdCidadao) {
        String sql = "SELECT COUNT(*) FROM tb_agendamento WHERE cd_cidadao = ?";
        return SequenceUtil.contar(dataSource, sql, cdCidadao);
    }

    private void preencherCampos(PreparedStatement ps, Cidadao cidadao, int inicio) throws SQLException {
        ps.setString(inicio, cidadao.getNmCidadao());
        ps.setString(inicio + 1, cidadao.getNrCpf());
        if (cidadao.getDsEmail() == null || cidadao.getDsEmail().isBlank()) {
            ps.setNull(inicio + 2, Types.VARCHAR);
        } else {
            ps.setString(inicio + 2, cidadao.getDsEmail());
        }
        ps.setString(inicio + 3, cidadao.getNrTelefone());
        ps.setDate(inicio + 4, Date.valueOf(cidadao.getDtNascimento()));
        ps.setString(inicio + 5, cidadao.getNmBairro());
    }

    private Cidadao montar(ResultSet rs) throws SQLException {
        Cidadao cidadao = new Cidadao();
        cidadao.setCdCidadao(rs.getLong("cd_cidadao"));
        cidadao.setNmCidadao(rs.getString("nm_cidadao"));
        cidadao.setNrCpf(rs.getString("nr_cpf"));
        cidadao.setDsEmail(rs.getString("ds_email"));
        cidadao.setNrTelefone(rs.getString("nr_telefone"));
        cidadao.setDtNascimento(rs.getDate("dt_nascimento").toLocalDate());
        cidadao.setNmBairro(rs.getString("nm_bairro"));
        return cidadao;
    }
}
