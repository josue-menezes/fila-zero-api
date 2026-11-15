package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.dto.AgendamentoRequestDTO;
import br.com.fiap.filazero.dto.AgendamentoResponseDTO;
import br.com.fiap.filazero.dto.CidadaoRequestDTO;
import br.com.fiap.filazero.dto.CidadaoResponseDTO;
import br.com.fiap.filazero.service.AgendamentoService;
import br.com.fiap.filazero.service.CidadaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas REST dos cidadãos e das rotas aninhadas do 1:N (cidadão -> agendamentos).
 */
@RestController
@RequestMapping("/cidadaos")
public class CidadaoController {

    private final CidadaoService cidadaoService;
    private final AgendamentoService agendamentoService;

    public CidadaoController(CidadaoService cidadaoService, AgendamentoService agendamentoService) {
        this.cidadaoService = cidadaoService;
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<CidadaoResponseDTO> cadastrar(@Valid @RequestBody CidadaoRequestDTO dto) {
        CidadaoResponseDTO criado = cidadaoService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getCdCidadao()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<CidadaoResponseDTO>> listar() {
        return ResponseEntity.ok(cidadaoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CidadaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cidadaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CidadaoResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CidadaoRequestDTO dto) {
        return ResponseEntity.ok(cidadaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cidadaoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Rota aninhada do 1:N: agendamentos do cidadão. */
    @GetMapping("/{id}/agendamentos")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentos(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.listarPorCidadao(id));
    }

    /** Rota aninhada do 1:N: o cidadão agenda um atendimento. */
    @PostMapping("/{id}/agendamentos")
    public ResponseEntity<AgendamentoResponseDTO> agendar(@PathVariable Long id,
                                                          @Valid @RequestBody AgendamentoRequestDTO dto) {
        AgendamentoResponseDTO criado = agendamentoService.agendar(id, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/agendamentos/{id}").buildAndExpand(criado.getCdAgendamento()).toUri();
        return ResponseEntity.created(location).body(criado);
    }
}
