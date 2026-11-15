package br.com.fiap.filazero.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para cadastrar ou atualizar uma unidade.
 */
public class UnidadeRequestDTO {

    @NotBlank(message = "O nome da unidade é obrigatório")
    @Size(max = 100, message = "O nome da unidade deve ter no máximo 100 caracteres")
    private String nmUnidade;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres")
    private String dsEndereco;

    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres")
    private String nmBairro;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 60, message = "A cidade deve ter no máximo 60 caracteres")
    private String nmCidade;

    @NotNull(message = "O número de guichês é obrigatório")
    @Positive(message = "O número de guichês deve ser maior que zero")
    @Max(value = 999, message = "O número de guichês deve ser no máximo 999")
    private Integer guiches;

    @NotNull(message = "A hora de abertura é obrigatória")
    @Min(value = 0, message = "A hora de abertura deve estar entre 0 e 23")
    @Max(value = 23, message = "A hora de abertura deve estar entre 0 e 23")
    private Integer hrAbertura;

    @NotNull(message = "A hora de fechamento é obrigatória")
    @Min(value = 1, message = "A hora de fechamento deve estar entre 1 e 23")
    @Max(value = 23, message = "A hora de fechamento deve estar entre 1 e 23")
    private Integer hrFechamento;

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
