package br.com.fiap.filazero.service;

import br.com.fiap.filazero.dao.AgendamentoDAO;
import br.com.fiap.filazero.dao.UnidadeDAO;
import br.com.fiap.filazero.dto.HorarioDisponivelDTO;
import br.com.fiap.filazero.dto.UnidadeRequestDTO;
import br.com.fiap.filazero.dto.UnidadeResponseDTO;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.RegistroComDependentesException;
import br.com.fiap.filazero.exception.RegistroDuplicadoException;
import br.com.fiap.filazero.model.Unidade;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regras e fluxo das unidades: o controller chama o service,
 * o service valida as regras e chama o DAO.
 */
@Service
public class UnidadeService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final UnidadeDAO unidadeDAO;
    private final AgendamentoDAO agendamentoDAO;
    private final ModelMapper modelMapper;

    public UnidadeService(UnidadeDAO unidadeDAO, AgendamentoDAO agendamentoDAO, ModelMapper modelMapper) {
        this.unidadeDAO = unidadeDAO;
        this.agendamentoDAO = agendamentoDAO;
        this.modelMapper = modelMapper;
    }

    public UnidadeResponseDTO cadastrar(UnidadeRequestDTO dto) {
        validarHorarioDeFuncionamento(dto);
        if (unidadeDAO.existeNome(dto.getNmUnidade(), null)) {
            throw new RegistroDuplicadoException("Já existe uma unidade com este nome.");
        }
        Unidade unidade = modelMapper.map(dto, Unidade.class);
        return paraResponse(unidadeDAO.inserir(unidade));
    }

    public List<UnidadeResponseDTO> listar() {
        return paraResponse(unidadeDAO.listar());
    }

    public UnidadeResponseDTO buscarPorId(Long cdUnidade) {
        return paraResponse(buscarOuFalhar(cdUnidade));
    }

    /** Unidades que oferecem um serviço (usada pelo ServicoController). */
    public List<UnidadeResponseDTO> listarPorServico(Long cdServico) {
        return paraResponse(unidadeDAO.listarPorServico(cdServico));
    }

    public UnidadeResponseDTO atualizar(Long cdUnidade, UnidadeRequestDTO dto) {
        buscarOuFalhar(cdUnidade);
        validarHorarioDeFuncionamento(dto);
        if (unidadeDAO.existeNome(dto.getNmUnidade(), cdUnidade)) {
            throw new RegistroDuplicadoException("Já existe outra unidade com este nome.");
        }
        Unidade unidade = modelMapper.map(dto, Unidade.class);
        unidade.setCdUnidade(cdUnidade);
        unidadeDAO.atualizar(unidade);
        return paraResponse(unidade);
    }

    public void remover(Long cdUnidade) {
        buscarOuFalhar(cdUnidade);
        // RN07: unidade com agendamentos, atendentes ou serviços vinculados não pode ser removida
        if (unidadeDAO.contarDependentes(cdUnidade) > 0) {
            throw new RegistroComDependentesException(
                    "Esta unidade possui agendamentos, atendentes ou serviços vinculados e não pode ser removida.");
        }
        unidadeDAO.remover(cdUnidade);
    }

    /**
     * Monta os horários de um dia (de 30 em 30 minutos, da abertura ao fechamento)
     * com as vagas restantes. Vagas = guichês - agendamentos ativos no horário (RN01).
     */
    public List<HorarioDisponivelDTO> listarHorarios(Long cdUnidade, LocalDate data) {
        Unidade unidade = buscarOuFalhar(cdUnidade);
        Map<LocalDateTime, Integer> ocupacao = agendamentoDAO.contarOcupacaoDoDia(cdUnidade, data);
        LocalDateTime agora = Relogio.agora();

        List<HorarioDisponivelDTO> horarios = new ArrayList<>();
        LocalDateTime horario = data.atTime(unidade.getHrAbertura(), 0);
        LocalDateTime fechamento = data.atTime(unidade.getHrFechamento(), 0);
        while (horario.isBefore(fechamento)) {
            int ocupados = ocupacao.getOrDefault(horario, 0);
            int vagas = Math.max(unidade.getGuiches() - ocupados, 0);
            horarios.add(new HorarioDisponivelDTO(horario.format(FORMATO_HORA), horario, vagas,
                    vagas == 0, !horario.isAfter(agora)));
            horario = horario.plusMinutes(30);
        }
        return horarios;
    }

    /** Busca a unidade ou lança 404. Também usada pelos outros services. */
    Unidade buscarOuFalhar(Long cdUnidade) {
        return unidadeDAO.buscarPorId(cdUnidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade " + cdUnidade + " não encontrada."));
    }

    private void validarHorarioDeFuncionamento(UnidadeRequestDTO dto) {
        if (dto.getHrFechamento() <= dto.getHrAbertura()) {
            throw new DadosInvalidosException("A hora de fechamento deve ser maior que a hora de abertura.");
        }
    }

    private UnidadeResponseDTO paraResponse(Unidade unidade) {
        return modelMapper.map(unidade, UnidadeResponseDTO.class);
    }

    private List<UnidadeResponseDTO> paraResponse(List<Unidade> unidades) {
        List<UnidadeResponseDTO> resposta = new ArrayList<>();
        for (Unidade unidade : unidades) {
            resposta.add(paraResponse(unidade));
        }
        return resposta;
    }
}
