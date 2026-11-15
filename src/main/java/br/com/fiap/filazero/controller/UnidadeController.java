package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.dto.AgendamentoResponseDTO;
import br.com.fiap.filazero.dto.HorarioDisponivelDTO;
import br.com.fiap.filazero.dto.UnidadeRequestDTO;
import br.com.fiap.filazero.dto.UnidadeResponseDTO;
import br.com.fiap.filazero.service.AgendamentoService;
import br.com.fiap.filazero.service.UnidadeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Rotas REST das unidades de atendimento.
 */
@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;
    private final AgendamentoService agendamentoService;

    public UnidadeController(UnidadeService unidadeService, AgendamentoService agendamentoService) {
        this.unidadeService = unidadeService;
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<UnidadeResponseDTO> cadastrar(@Valid @RequestBody UnidadeRequestDTO dto) {
        UnidadeResponseDTO criada = unidadeService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getCdUnidade()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<UnidadeResponseDTO>> listar() {
        return ResponseEntity.ok(unidadeService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(unidadeService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UnidadeRequestDTO dto) {
        return ResponseEntity.ok(unidadeService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        unidadeService.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Rota aninhada do 1:N. Ex.: /unidades/2/agendamentos?data=2026-11-19 (data opcional). */
    @GetMapping("/{id}/agendamentos")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentos(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.listarPorUnidade(id, data));
    }

    /** Horários do dia com vagas e lotação. Ex.: /unidades/2/horarios?data=2026-11-19 */
    @GetMapping("/{id}/horarios")
    public ResponseEntity<List<HorarioDisponivelDTO>> listarHorarios(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(unidadeService.listarHorarios(id, data));
    }
}
