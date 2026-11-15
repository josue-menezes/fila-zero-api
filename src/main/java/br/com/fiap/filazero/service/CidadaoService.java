package br.com.fiap.filazero.service;

import br.com.fiap.filazero.dao.CidadaoDAO;
import br.com.fiap.filazero.dto.CidadaoRequestDTO;
import br.com.fiap.filazero.dto.CidadaoResponseDTO;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.RegistroComDependentesException;
import br.com.fiap.filazero.exception.RegistroDuplicadoException;
import br.com.fiap.filazero.model.Cidadao;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras e fluxo dos cidadãos.
 */
@Service
public class CidadaoService {

    private final CidadaoDAO cidadaoDAO;
    private final ModelMapper modelMapper;

    public CidadaoService(CidadaoDAO cidadaoDAO, ModelMapper modelMapper) {
        this.cidadaoDAO = cidadaoDAO;
        this.modelMapper = modelMapper;
    }

    public CidadaoResponseDTO cadastrar(CidadaoRequestDTO dto) {
        validarDuplicidade(dto, null);
        Cidadao cidadao = modelMapper.map(dto, Cidadao.class);
        return paraResponse(cidadaoDAO.inserir(cidadao));
    }

    public List<CidadaoResponseDTO> listar() {
        List<CidadaoResponseDTO> resposta = new ArrayList<>();
        for (Cidadao cidadao : cidadaoDAO.listar()) {
            resposta.add(paraResponse(cidadao));
        }
        return resposta;
    }

    public CidadaoResponseDTO buscarPorId(Long cdCidadao) {
        return paraResponse(buscarOuFalhar(cdCidadao));
    }

    public CidadaoResponseDTO atualizar(Long cdCidadao, CidadaoRequestDTO dto) {
        buscarOuFalhar(cdCidadao);
        validarDuplicidade(dto, cdCidadao);
        Cidadao cidadao = modelMapper.map(dto, Cidadao.class);
        cidadao.setCdCidadao(cdCidadao);
        cidadaoDAO.atualizar(cidadao);
        return paraResponse(cidadao);
    }

    public void remover(Long cdCidadao) {
        buscarOuFalhar(cdCidadao);
        // RN07: cidadão com agendamentos não pode ser removido
        if (cidadaoDAO.contarAgendamentos(cdCidadao) > 0) {
            throw new RegistroComDependentesException(
                    "Este cidadão possui agendamentos e não pode ser removido.");
        }
        cidadaoDAO.remover(cdCidadao);
    }

    Cidadao buscarOuFalhar(Long cdCidadao) {
        return cidadaoDAO.buscarPorId(cdCidadao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão " + cdCidadao + " não encontrado."));
    }

    /** CPF e e-mail são únicos no banco: avisa com 409 antes de tentar gravar. */
    private void validarDuplicidade(CidadaoRequestDTO dto, Long ignorarCodigo) {
        if (cidadaoDAO.existeCpf(dto.getNrCpf(), ignorarCodigo)) {
            throw new RegistroDuplicadoException("Já existe um cidadão cadastrado com este CPF.");
        }
        if (dto.getDsEmail() != null && !dto.getDsEmail().isBlank()
                && cidadaoDAO.existeEmail(dto.getDsEmail(), ignorarCodigo)) {
            throw new RegistroDuplicadoException("Já existe um cidadão cadastrado com este e-mail.");
        }
    }

    private CidadaoResponseDTO paraResponse(Cidadao cidadao) {
        return modelMapper.map(cidadao, CidadaoResponseDTO.class);
    }
}
