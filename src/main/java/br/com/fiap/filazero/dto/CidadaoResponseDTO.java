package br.com.fiap.filazero.dto;

import java.time.LocalDate;

/**
 * Dados de saída de um cidadão.
 */
public class CidadaoResponseDTO {

    private Long cdCidadao;
    private String nmCidadao;
    private String nrCpf;
    private String dsEmail;
    private String nrTelefone;
    private LocalDate dtNascimento;
    private String nmBairro;

    public Long getCdCidadao() { return cdCidadao; }
    public void setCdCidadao(Long cdCidadao) { this.cdCidadao = cdCidadao; }

    public String getNmCidadao() { return nmCidadao; }
    public void setNmCidadao(String nmCidadao) { this.nmCidadao = nmCidadao; }

    public String getNrCpf() { return nrCpf; }
    public void setNrCpf(String nrCpf) { this.nrCpf = nrCpf; }

    public String getDsEmail() { return dsEmail; }
    public void setDsEmail(String dsEmail) { this.dsEmail = dsEmail; }

    public String getNrTelefone() { return nrTelefone; }
    public void setNrTelefone(String nrTelefone) { this.nrTelefone = nrTelefone; }

    public LocalDate getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(LocalDate dtNascimento) { this.dtNascimento = dtNascimento; }

    public String getNmBairro() { return nmBairro; }
    public void setNmBairro(String nmBairro) { this.nmBairro = nmBairro; }
}
