package br.com.fiap.filazero.dto;

/**
 * Dados de saída de um serviço.
 */
public class ServicoResponseDTO {

    private Long cdServico;
    private String nmServico;
    private String dsServico;
    private String dsDocumentos;
    private Integer nrDuracaoMin;

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
