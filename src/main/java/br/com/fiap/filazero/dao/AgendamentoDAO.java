package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDeDadosException;
import br.com.fiap.filazero.model.Agendamento;
import br.com.fiap.filazero.model.Cidadao;
import br.com.fiap.filazero.model.Servico;
import br.com.fiap.filazero.model.StatusAgendamento;
import br.com.fiap.filazero.model.Unidade;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso à tabela tb_agendamento com JDBC puro.
 * As consultas já trazem, por JOIN, o nome do cidadão, da unidade e do serviço.
 */
@Repository
public class AgendamentoDAO {

    /** SELECT base: agendamento + dados resumidos de cidadão, unidade e serviço. */
    private static final String SELECT_BASE =
            "SELECT a.cd_agendamento, a.cd_atendente, a.dt_agendamento, a.dt_criacao, "
                    + "a.st_agendamento, a.nr_espera_min, "
                    + "c.cd_cidadao, c.nm_cidadao, "
                    + "u.cd_unidade, u.nm_unidade, "
                    + "s.cd_servico, s.nm_servico, s.ds_documentos "
                    + "FROM tb_agendamento a "
                    + "INNER JOIN tb_cidadao c ON c.cd_cidadao = a.cd_cidadao "
                    + "INNER JOIN tb_unidade u ON u.cd_unidade = a.cd_unidade "
                    + "INNER JOIN tb_servico s ON s.cd_servico = a.cd_servico ";

    /** Status que ainda ocupam vaga no horário. */
    private static final String STATUS_ATIVOS = "('AGENDADO', 'CONFIRMADO')";

    private final DataSource dataSource;

    public AgendamentoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Agendamento agendamento) {
        String sql = "INSERT INTO tb_agendamento (cd_agendamento, cd_cidadao, cd_unidade, cd_servico, "
                + "dt_agendamento, dt_criacao, st_agendamento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            Long novoCodigo = SequenceUtil.proximoValor(conexao, "sq_tb_agendamento");
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, novoCodigo);
                ps.setLong(2, agendamento.getCidadao().getCdCidadao());
                ps.setLong(3, agendamento.getUnidade().getCdUnidade());
                ps.setLong(4, agendamento.getServico().getCdServico());
                ps.setTimestamp(5, Timestamp.valueOf(agendamento.getDtAgendamento()));
                ps.setTimestamp(6, Timestamp.valueOf(agendamento.getDtCriacao()));
                ps.setString(7, agendamento.getStatus().name());
                ps.executeUpdate();
            }
            return novoCodigo;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar agendamento", e);
        }
    }

    public List<Agendamento> listar() {
        return consultarLista(SELECT_BASE + "ORDER BY a.dt_agendamento");
    }

    public Optional<Agendamento> buscarPorId(Long cdAgendamento) {
        List<Agendamento> resultado = consultarLista(SELECT_BASE + "WHERE a.cd_agendamento = ?", cdAgendamento);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    /** Rota aninhada do 1:N: agendamentos de um cidadão. */
    public List<Agendamento> listarPorCidadao(Long cdCidadao) {
        return consultarLista(SELECT_BASE + "WHERE a.cd_cidadao = ? ORDER BY a.dt_agendamento", cdCidadao);
    }

    /** Agendamentos de uma unidade; se a data vier preenchida, filtra só aquele dia. */
    public List<Agendamento> listarPorUnidade(Long cdUnidade, LocalDate data) {
        if (data == null) {
            return consultarLista(SELECT_BASE + "WHERE a.cd_unidade = ? ORDER BY a.dt_agendamento", cdUnidade);
        }
        return consultarLista(SELECT_BASE
                        + "WHERE a.cd_unidade = ? AND a.dt_agendamento >= ? AND a.dt_agendamento < ? "
                        + "ORDER BY a.dt_agendamento",
                cdUnidade, inicioDoDia(data), inicioDoDia(data.plusDays(1)));
    }

    public void atualizar(Agendamento agendamento) {
        String sql = "UPDATE tb_agendamento SET dt_agendamento = ?, st_agendamento = ?, "
                + "cd_atendente = ?, nr_espera_min = ? WHERE cd_agendamento = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(agendamento.getDtAgendamento()));
            ps.setString(2, agendamento.getStatus().name());
            if (agendamento.getCdAtendente() == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setLong(3, agendamento.getCdAtendente());
            }
            if (agendamento.getEsperaMin() == null) {
                ps.setNull(4, Types.NUMERIC);
            } else {
                ps.setInt(4, agendamento.getEsperaMin());
            }
            ps.setLong(5, agendamento.getCdAgendamento());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar agendamento", e);
        }
    }

    public void remover(Long cdAgendamento) {
        String sql = "DELETE FROM tb_agendamento WHERE cd_agendamento = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdAgendamento);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao remover agendamento", e);
        }
    }

    /** RN01: quantos agendamentos ativos a unidade já tem naquele horário exato. */
    public int contarAtivosNoHorario(Long cdUnidade, LocalDateTime horario, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_agendamento WHERE cd_unidade = ? AND dt_agendamento = ? "
                + "AND st_agendamento IN " + STATUS_ATIVOS + " AND cd_agendamento <> ?";
        return SequenceUtil.contar(dataSource, sql, cdUnidade, Timestamp.valueOf(horario),
                ignorarCodigo == null ? -1L : ignorarCodigo);
    }

    /** RN04: o cidadão já tem agendamento ativo para este serviço? */
    public boolean existeAtivoMesmoServico(Long cdCidadao, Long cdServico, Long ignorarCodigo) {
        String sql = "SELECT COUNT(*) FROM tb_agendamento WHERE cd_cidadao = ? AND cd_servico = ? "
                + "AND st_agendamento IN " + STATUS_ATIVOS + " AND cd_agendamento <> ?";
        return SequenceUtil.contar(dataSource, sql, cdCidadao, cdServico,
                ignorarCodigo == null ? -1L : ignorarCodigo) > 0;
    }

    /** Ocupação de cada horário de um dia: horário -> quantidade de agendamentos ativos. */
    public Map<LocalDateTime, Integer> contarOcupacaoDoDia(Long cdUnidade, LocalDate data) {
        String sql = "SELECT dt_agendamento, COUNT(*) AS total FROM tb_agendamento "
                + "WHERE cd_unidade = ? AND dt_agendamento >= ? AND dt_agendamento < ? "
                + "AND st_agendamento IN " + STATUS_ATIVOS + " GROUP BY dt_agendamento";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, cdUnidade);
            ps.setTimestamp(2, inicioDoDia(data));
            ps.setTimestamp(3, inicioDoDia(data.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDateTime, Integer> ocupacao = new HashMap<>();
                while (rs.next()) {
                    ocupacao.put(rs.getTimestamp("dt_agendamento").toLocalDateTime(), rs.getInt("total"));
                }
                return ocupacao;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao consultar horários", e);
        }
    }

    private List<Agendamento> consultarLista(String sql, Object... parametros) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Agendamento> agendamentos = new ArrayList<>();
                while (rs.next()) {
                    agendamentos.add(montar(rs));
                }
                return agendamentos;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao consultar agendamentos", e);
        }
    }

    private Timestamp inicioDoDia(LocalDate data) {
        return Timestamp.valueOf(data.atStartOfDay());
    }

    private Agendamento montar(ResultSet rs) throws SQLException {
        Cidadao cidadao = new Cidadao();
        cidadao.setCdCidadao(rs.getLong("cd_cidadao"));
        cidadao.setNmCidadao(rs.getString("nm_cidadao"));

        Unidade unidade = new Unidade();
        unidade.setCdUnidade(rs.getLong("cd_unidade"));
        unidade.setNmUnidade(rs.getString("nm_unidade"));

        Servico servico = new Servico();
        servico.setCdServico(rs.getLong("cd_servico"));
        servico.setNmServico(rs.getString("nm_servico"));
        servico.setDsDocumentos(rs.getString("ds_documentos"));

        Agendamento agendamento = new Agendamento();
        agendamento.setCdAgendamento(rs.getLong("cd_agendamento"));
        agendamento.setCidadao(cidadao);
        agendamento.setUnidade(unidade);
        agendamento.setServico(servico);

        long cdAtendente = rs.getLong("cd_atendente");
        agendamento.setCdAtendente(rs.wasNull() ? null : cdAtendente);

        agendamento.setDtAgendamento(rs.getTimestamp("dt_agendamento").toLocalDateTime());
        agendamento.setDtCriacao(rs.getTimestamp("dt_criacao").toLocalDateTime());
        agendamento.setStatus(StatusAgendamento.valueOf(rs.getString("st_agendamento")));

        int espera = rs.getInt("nr_espera_min");
        agendamento.setEsperaMin(rs.wasNull() ? null : espera);
        return agendamento;
    }
}
