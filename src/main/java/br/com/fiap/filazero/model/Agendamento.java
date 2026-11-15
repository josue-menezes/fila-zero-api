package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Horário marcado por um cidadão, para um serviço, numa unidade.
 * Tabela: tb_agendamento. Fica no "lado N" dos relacionamentos com
 * Cidadão, Unidade e Serviço.
 */
public class Agendamento {

    private Long cdAgendamento;
    private Cidadao cidadao;
    private Unidade unidade;
    private Servico servico;
    /** Atendente que realizou o atendimento (preenchido só no atendimento). */
    private Long cdAtendente;
    private LocalDateTime dtAgendamento;
    private LocalDateTime dtCriacao;
    private StatusAgendamento status;
    /** Espera real em minutos (preenchida só no atendimento). */
    private Integer esperaMin;

    public Agendamento() {
    }

    public Long getCdAgendamento() { return cdAgendamento; }
    public void setCdAgendamento(Long cdAgendamento) { this.cdAgendamento = cdAgendamento; }

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public Unidade getUnidade() { return unidade; }
    public void setUnidade(Unidade unidade) { this.unidade = unidade; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public Long getCdAtendente() { return cdAtendente; }
    public void setCdAtendente(Long cdAtendente) { this.cdAtendente = cdAtendente; }

    public LocalDateTime getDtAgendamento() { return dtAgendamento; }
    public void setDtAgendamento(LocalDateTime dtAgendamento) { this.dtAgendamento = dtAgendamento; }

    public LocalDateTime getDtCriacao() { return dtCriacao; }
    public void setDtCriacao(LocalDateTime dtCriacao) { this.dtCriacao = dtCriacao; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public Integer getEsperaMin() { return esperaMin; }
    public void setEsperaMin(Integer esperaMin) { this.esperaMin = esperaMin; }
}
