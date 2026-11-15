package br.com.fiap.filazero.model;

/**
 * Serviço público que pode ser agendado (ex.: Emissão de RG).
 * Tabela: tb_servico.
 */
public class Servico {

    private Long cdServico;
    private String nmServico;
    private String dsServico;
    /** Documentos que o cidadão precisa levar. */
    private String dsDocumentos;
    /** Duração média do atendimento, em minutos. */
    private Integer nrDuracaoMin;

    public Servico() {
    }

    public Long getCdServico() { return cdServico; }
    public void setCdServico(Long cdServico) { this.cdServico = cdServico; }

    public String getNmServico() { return nmServico; }
    public void setNmServico(String nmServico) { this.nmServico = nmServico; }

    public String getDsServico() { return dsServico; }
    public void setDsServico(String dsServico) { this.dsServico = dsServico; }

    public String getDsDocumentos() { return dsDocumentos; }
    public void setDsDocumentos(String dsDocumentos) { this.dsDocumentos = dsDocumentos; }

    public Integer getNrDuracaoMin() { return nrDuracaoMin; }
    public void setNrDuracaoMin(Integer nrDuracaoMin) { this.nrDuracaoMin = nrDuracaoMin; }
}
