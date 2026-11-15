package br.com.fiap.filazero.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para cadastrar ou atualizar um serviço.
 */
public class ServicoRequestDTO {

    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 80, message = "O nome do serviço deve ter no máximo 80 caracteres")
    private String nmServico;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String dsServico;

    @NotBlank(message = "Os documentos necessários são obrigatórios")
    @Size(max = 255, message = "Os documentos devem ter no máximo 255 caracteres")
    private String dsDocumentos;

    @NotNull(message = "A duração do atendimento é obrigatória")
    @Positive(message = "A duração do atendimento deve ser maior que zero")
    @Max(value = 999, message = "A duração do atendimento deve ser no máximo 999 minutos")
    private Integer nrDuracaoMin;

    public String getNmServico() { return nmServico; }
    public void setNmServico(String nmServico) { this.nmServico = nmServico; }

    public String getDsServico() { return dsServico; }
    public void setDsServico(String dsServico) { this.dsServico = dsServico; }

    public String getDsDocumentos() { return dsDocumentos; }
    public void setDsDocumentos(String dsDocumentos) { this.dsDocumentos = dsDocumentos; }

    public Integer getNrDuracaoMin() { return nrDuracaoMin; }
    public void setNrDuracaoMin(Integer nrDuracaoMin) { this.nrDuracaoMin = nrDuracaoMin; }
}
