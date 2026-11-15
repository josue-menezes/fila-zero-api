package br.com.fiap.filazero.service;

import br.com.fiap.filazero.dao.AgendamentoDAO;
import br.com.fiap.filazero.dto.AgendamentoRequestDTO;
import br.com.fiap.filazero.dto.AgendamentoResponseDTO;
import br.com.fiap.filazero.dto.AgendamentoUpdateDTO;
import br.com.fiap.filazero.exception.AgendamentoDuplicadoException;
import br.com.fiap.filazero.exception.AgendamentoFinalizadoException;
import br.com.fiap.filazero.exception.CancelamentoForaDoPrazoException;
import br.com.fiap.filazero.exception.HorarioInvalidoException;
import br.com.fiap.filazero.exception.HorarioLotadoException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.ServicoIndisponivelException;
import br.com.fiap.filazero.model.Agendamento;
import br.com.fiap.filazero.model.Cidadao;
import br.com.fiap.filazero.model.Servico;
import br.com.fiap.filazero.model.StatusAgendamento;
import br.com.fiap.filazero.model.Unidade;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Coração do Fila Zero: agendar, confirmar, cancelar e reagendar,
 * aplicando as regras de negócio RN01 a RN06.
 */
@Service
public class AgendamentoService {

    /** RN05: prazo mínimo, em horas, para cancelar. */
    private static final int HORAS_MINIMAS_PARA_CANCELAR = 2;

    private final AgendamentoDAO agendamentoDAO;
    private final CidadaoService cidadaoService;
    private final UnidadeService unidadeService;
    private final ServicoService servicoService;
    private final ModelMapper modelMapper;

    public AgendamentoService(AgendamentoDAO agendamentoDAO, CidadaoService cidadaoService,
                              UnidadeService unidadeService, ServicoService servicoService,
                              ModelMapper modelMapper) {
        this.agendamentoDAO = agendamentoDAO;
        this.cidadaoService = cidadaoService;
        this.unidadeService = unidadeService;
        this.servicoService = servicoService;
        this.modelMapper = modelMapper;
    }

    /** Cria um agendamento para o cidadão (rota aninhada POST /cidadaos/{id}/agendamentos). */
    public AgendamentoResponseDTO agendar(Long cdCidadao, AgendamentoRequestDTO dto) {
        Cidadao cidadao = cidadaoService.buscarOuFalhar(cdCidadao);
        Unidade unidade = unidadeService.buscarOuFalhar(dto.getCdUnidade());
        Servico servico = servicoService.buscarOuFalhar(dto.getCdServico());

        // RN02: a unidade precisa oferecer o serviço
        if (!servicoService.unidadeOferece(unidade.getCdUnidade(), servico.getCdServico())) {
            throw new ServicoIndisponivelException(
                    "A unidade " + unidade.getNmUnidade() + " não oferece o serviço " + servico.getNmServico() + ".");
        }

        // RN03: horário válido para a unidade
        validarHorario(unidade, dto.getDtAgendamento());

        // RN04: um agendamento ativo por serviço para cada cidadão
        if (agendamentoDAO.existeAtivoMesmoServico(cdCidadao, servico.getCdServico(), null)) {
            throw new AgendamentoDuplicadoException(
                    "Você já tem um agendamento ativo para " + servico.getNmServico()
                            + ". Cancele o anterior antes de marcar outro.");
        }

        // RN01: o horário não pode estar lotado
        validarLotacao(unidade, dto.getDtAgendamento(), null);

        Agendamento agendamento = new Agendamento();
        agendamento.setCidadao(cidadao);
        agendamento.setUnidade(unidade);
        agendamento.setServico(servico);
        agendamento.setDtAgendamento(dto.getDtAgendamento());
        agendamento.setDtCriacao(Relogio.agora().withNano(0));
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        Long novoCodigo = agendamentoDAO.inserir(agendamento);
        return buscarPorId(novoCodigo);
    }

    public List<AgendamentoResponseDTO> listar() {
        return paraResponse(agendamentoDAO.listar());
    }

    public AgendamentoResponseDTO buscarPorId(Long cdAgendamento) {
        return paraResponse(buscarOuFalhar(cdAgendamento));
    }

    public List<AgendamentoResponseDTO> listarPorCidadao(Long cdCidadao) {
        cidadaoService.buscarOuFalhar(cdCidadao);
        return paraResponse(agendamentoDAO.listarPorCidadao(cdCidadao));
    }

    public List<AgendamentoResponseDTO> listarPorUnidade(Long cdUnidade, LocalDate data) {
        unidadeService.buscarOuFalhar(cdUnidade);
        return paraResponse(agendamentoDAO.listarPorUnidade(cdUnidade, data));
    }

    /** Confirma, cancela, reagenda ou registra o resultado de um agendamento. */
    public AgendamentoResponseDTO atualizar(Long cdAgendamento, AgendamentoUpdateDTO dto) {
        Agendamento agendamento = buscarOuFalhar(cdAgendamento);

        // RN06: status final não muda mais
        if (agendamento.getStatus().isFinal()) {
            throw new AgendamentoFinalizadoException(
                    "Este agendamento já está " + agendamento.getStatus() + " e não pode mais ser alterado.");
        }

        StatusAgendamento novoStatus = dto.getStatus();

        if (novoStatus == StatusAgendamento.CANCELADO) {
            // RN05: cancelamento só até 2 horas antes
            LocalDateTime limite = agendamento.getDtAgendamento().minusHours(HORAS_MINIMAS_PARA_CANCELAR);
            if (Relogio.agora().isAfter(limite)) {
                throw new CancelamentoForaDoPrazoException(
                        "O cancelamento só é permitido até " + HORAS_MINIMAS_PARA_CANCELAR
                                + " horas antes do horário marcado.");
            }
        } else if (novoStatus.isAtivo() && dto.getDtAgendamento() != null
                && !dto.getDtAgendamento().equals(agendamento.getDtAgendamento())) {
            // Reagendamento: o novo horário passa pelas mesmas regras de um agendamento novo
            Unidade unidade = unidadeService.buscarOuFalhar(agendamento.getUnidade().getCdUnidade());
            validarHorario(unidade, dto.getDtAgendamento());
            validarLotacao(unidade, dto.getDtAgendamento(), cdAgendamento);
            agendamento.setDtAgendamento(dto.getDtAgendamento());
        }

        agendamento.setStatus(novoStatus);
        agendamentoDAO.atualizar(agendamento);
        return buscarPorId(cdAgendamento);
    }

    public void remover(Long cdAgendamento) {
        buscarOuFalhar(cdAgendamento);
        agendamentoDAO.remover(cdAgendamento);
    }

    private Agendamento buscarOuFalhar(Long cdAgendamento) {
        return agendamentoDAO.buscarPorId(cdAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Agendamento " + cdAgendamento + " não encontrado."));
    }

    /** RN03: horário cheio ou :30 e dentro do funcionamento da unidade. */
    private void validarHorario(Unidade unidade, LocalDateTime horario) {
        int minuto = horario.getMinute();
        if ((minuto != 0 && minuto != 30) || horario.getSecond() != 0) {
            throw new HorarioInvalidoException("Os horários são de 30 em 30 minutos (ex.: 14:00 ou 14:30).");
        }
        int hora = horario.getHour();
        if (hora < unidade.getHrAbertura() || hora >= unidade.getHrFechamento()) {
            throw new HorarioInvalidoException(
                    "A unidade " + unidade.getNmUnidade() + " atende das " + unidade.getHrAbertura()
                            + "h às " + unidade.getHrFechamento() + "h.");
        }
    }

    /** RN01: agendamentos ativos no horário não podem passar do número de guichês. */
    private void validarLotacao(Unidade unidade, LocalDateTime horario, Long ignorarCodigo) {
        int ocupados = agendamentoDAO.contarAtivosNoHorario(unidade.getCdUnidade(), horario, ignorarCodigo);
        if (ocupados >= unidade.getGuiches()) {
            throw new HorarioLotadoException("Este horário já está lotado nesta unidade. Escolha outro horário.");
        }
    }

    private AgendamentoResponseDTO paraResponse(Agendamento agendamento) {
        return modelMapper.map(agendamento, AgendamentoResponseDTO.class);
    }

    private List<AgendamentoResponseDTO> paraResponse(List<Agendamento> agendamentos) {
        List<AgendamentoResponseDTO> resposta = new ArrayList<>();
        for (Agendamento agendamento : agendamentos) {
            resposta.add(paraResponse(agendamento));
        }
        return resposta;
    }
}
