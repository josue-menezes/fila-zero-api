package br.com.fiap.filazero.model;

/**
 * Unidade de atendimento ao cidadão (estilo Poupatempo).
 * Tabela: tb_unidade.
 */
public class Unidade {

    private Long cdUnidade;
    private String nmUnidade;
    private String dsEndereco;
    private String nmBairro;
    private String nmCidade;
    /** Quantos agendamentos cabem no mesmo horário (coluna nr_guiches). */
    private Integer guiches;
    private Integer hrAbertura;
    private Integer hrFechamento;

    public Unidade() {
    }

    public Long getCdUnidade() { return cdUnidade; }
    public void setCdUnidade(Long cdUnidade) { this.cdUnidade = cdUnidade; }

    public String getNmUnidade() { return nmUnidade; }
    public void setNmUnidade(String nmUnidade) { this.nmUnidade = nmUnidade; }

    public String getDsEndereco() { return dsEndereco; }
    public void setDsEndereco(String dsEndereco) { this.dsEndereco = dsEndereco; }

    public String getNmBairro() { return nmBairro; }
    public void setNmBairro(String nmBairro) { this.nmBairro = nmBairro; }

    public String getNmCidade() { return nmCidade; }
    public void setNmCidade(String nmCidade) { this.nmCidade = nmCidade; }

    public Integer getGuiches() { return guiches; }
    public void setGuiches(Integer guiches) { this.guiches = guiches; }

    public Integer getHrAbertura() { return hrAbertura; }
    public void setHrAbertura(Integer hrAbertura) { this.hrAbertura = hrAbertura; }

    public Integer getHrFechamento() { return hrFechamento; }
    public void setHrFechamento(Integer hrFechamento) { this.hrFechamento = hrFechamento; }
}
