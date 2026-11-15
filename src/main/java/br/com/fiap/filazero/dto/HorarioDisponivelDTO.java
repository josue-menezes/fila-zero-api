package br.com.fiap.filazero.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Um horário de atendimento de uma unidade num dia, com as vagas que sobram.
 * Usado pelo front para riscar os horários "Lotado" (RN01).
 */
public class HorarioDisponivelDTO {

    /** Horário no formato HH:mm (ex.: 14:00). */
    private String horario;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    private int vagas;
    private boolean lotado;
    /** Horário que já passou (não pode mais ser agendado). */
    private boolean passado;

    public HorarioDisponivelDTO(String horario, LocalDateTime dataHora, int vagas, boolean lotado, boolean passado) {
        this.horario = horario;
        this.dataHora = dataHora;
        this.vagas = vagas;
        this.lotado = lotado;
        this.passado = passado;
    }

    public String getHorario() { return horario; }
    public LocalDateTime getDataHora() { return dataHora; }
    public int getVagas() { return vagas; }
    public boolean isLotado() { return lotado; }
    public boolean isPassado() { return passado; }
}
