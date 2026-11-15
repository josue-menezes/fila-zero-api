package br.com.fiap.filazero.model;

import java.time.LocalDate;

/**
 * Cidadão que usa o Fila Zero para agendar atendimentos.
 * Tabela: tb_cidadao. Um cidadão tem vários agendamentos (1:N).
 */
public class Cidadao {

    private Long cdCidadao;
    private String nmCidadao;
    /** CPF só com números (11 dígitos). */
    private String nrCpf;
    private String dsEmail;
    /** Celular com DDD, só números. */
    private String nrTelefone;
    private LocalDate dtNascimento;
    private String nmBairro;

    public Cidadao() {
    }

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
