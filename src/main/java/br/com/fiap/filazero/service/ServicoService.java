package br.com.fiap.filazero.service;

import br.com.fiap.filazero.dao.ServicoDAO;
import br.com.fiap.filazero.dto.ServicoRequestDTO;
import br.com.fiap.filazero.dto.ServicoResponseDTO;
import br.com.fiap.filazero.dto.UnidadeResponseDTO;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.RegistroComDependentesException;
import br.com.fiap.filazero.exception.RegistroDuplicadoException;
import br.com.fiap.filazero.model.Servico;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras e fluxo dos serviços públicos.
 */
@Service
public class ServicoService {

    private final ServicoDAO servicoDAO;
    private final UnidadeService unidadeService;
    private final ModelMapper modelMapper;

    public ServicoService(ServicoDAO servicoDAO, UnidadeService unidadeService, ModelMapper modelMapper) {
        this.servicoDAO = servicoDAO;
        this.unidadeService = unidadeService;
        this.modelMapper = modelMapper;
    }

    public ServicoResponseDTO cadastrar(ServicoRequestDTO dto) {
        if (servicoDAO.existeNome(dto.getNmServico(), null)) {
            throw new RegistroDuplicadoException("Já existe um serviço com este nome.");
        }
        Servico servico = modelMapper.map(dto, Servico.class);
        return paraResponse(servicoDAO.inserir(servico));
    }

    public List<ServicoResponseDTO> listar() {
        List<ServicoResponseDTO> resposta = new ArrayList<>();
        for (Servico servico : servicoDAO.listar()) {
            resposta.add(paraResponse(servico));
        }
        return resposta;
    }

    public ServicoResponseDTO buscarPorId(Long cdServico) {
        return paraResponse(buscarOuFalhar(cdServico));
    }

    /** Unidades que oferecem o serviço (relacionamento N:N). */
    public List<UnidadeResponseDTO> listarUnidades(Long cdServico) {
        buscarOuFalhar(cdServico);
        return unidadeService.listarPorServico(cdServico);
    }

    public ServicoResponseDTO atualizar(Long cdServico, ServicoRequestDTO dto) {
        buscarOuFalhar(cdServico);
        if (servicoDAO.existeNome(dto.getNmServico(), cdServico)) {
            throw new RegistroDuplicadoException("Já existe outro serviço com este nome.");
        }
        Servico servico = modelMapper.map(dto, Servico.class);
        servico.setCdServico(cdServico);
        servicoDAO.atualizar(servico);
        return paraResponse(servico);
    }

    public void remover(Long cdServico) {
        buscarOuFalhar(cdServico);
        // RN07: serviço com agendamentos ou oferecido em unidades não pode ser removido
        if (servicoDAO.contarDependentes(cdServico) > 0) {
            throw new RegistroComDependentesException(
                    "Este serviço possui agendamentos ou é oferecido em unidades e não pode ser removido.");
        }
        servicoDAO.remover(cdServico);
    }

    Servico buscarOuFalhar(Long cdServico) {
        return servicoDAO.buscarPorId(cdServico)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço " + cdServico + " não encontrado."));
    }

    boolean unidadeOferece(Long cdUnidade, Long cdServico) {
        return servicoDAO.unidadeOferece(cdUnidade, cdServico);
    }

    private ServicoResponseDTO paraResponse(Servico servico) {
        return modelMapper.map(servico, ServicoResponseDTO.class);
    }
}
