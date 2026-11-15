package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.StatusAgendamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Dados de saída de um agendamento, já com o resumo do cidadão,
 * da unidade e do serviço (o front não precisa fazer outras chamadas).
 */
public class AgendamentoResponseDTO {

    private Long cdAgendamento;
    private CidadaoResumoDTO cidadao;
    private UnidadeResumoDTO unidade;
    private ServicoResumoDTO servico;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtAgendamento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtCriacao;

    private StatusAgendamento status;
    private Integer esperaMin;

    public Long getCdAgendamento() { return cdAgendamento; }
    public void setCdAgendamento(Long cdAgendamento) { this.cdAgendamento = cdAgendamento; }

    public CidadaoResumoDTO getCidadao() { return cidadao; }
    public void setCidadao(CidadaoResumoDTO cidadao) { this.cidadao = cidadao; }

    public UnidadeResumoDTO getUnidade() { return unidade; }
    public void setUnidade(UnidadeResumoDTO unidade) { this.unidade = unidade; }

    public ServicoResumoDTO getServico() { return servico; }
    public void setServico(ServicoResumoDTO servico) { this.servico = servico; }

    public LocalDateTime getDtAgendamento() { return dtAgendamento; }
    public void setDtAgendamento(LocalDateTime dtAgendamento) { this.dtAgendamento = dtAgendamento; }

    public LocalDateTime getDtCriacao() { return dtCriacao; }
    public void setDtCriacao(LocalDateTime dtCriacao) { this.dtCriacao = dtCriacao; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public Integer getEsperaMin() { return esperaMin; }
    public void setEsperaMin(Integer esperaMin) { this.esperaMin = esperaMin; }

    /** Resumo do cidadão dentro do agendamento. */
    public static class CidadaoResumoDTO {
        private Long cdCidadao;
        private String nmCidadao;

        public Long getCdCidadao() { return cdCidadao; }
        public void setCdCidadao(Long cdCidadao) { this.cdCidadao = cdCidadao; }

        public String getNmCidadao() { return nmCidadao; }
        public void setNmCidadao(String nmCidadao) { this.nmCidadao = nmCidadao; }
    }

    /** Resumo da unidade dentro do agendamento. */
    public static class UnidadeResumoDTO {
        private Long cdUnidade;
        private String nmUnidade;

        public Long getCdUnidade() { return cdUnidade; }
        public void setCdUnidade(Long cdUnidade) { this.cdUnidade = cdUnidade; }

        public String getNmUnidade() { return nmUnidade; }
        public void setNmUnidade(String nmUnidade) { this.nmUnidade = nmUnidade; }
    }

    /** Resumo do serviço dentro do agendamento, com os documentos a levar. */
    public static class ServicoResumoDTO {
        private Long cdServico;
        private String nmServico;
        private String dsDocumentos;

        public Long getCdServico() { return cdServico; }
        public void setCdServico(Long cdServico) { this.cdServico = cdServico; }

        public String getNmServico() { return nmServico; }
        public void setNmServico(String nmServico) { this.nmServico = nmServico; }

        public String getDsDocumentos() { return dsDocumentos; }
        public void setDsDocumentos(String dsDocumentos) { this.dsDocumentos = dsDocumentos; }
    }
}
