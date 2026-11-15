package br.com.fiap.filazero.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padrão de TODO erro devolvido pela API.
 * Nunca contém stack trace nem detalhes do SQL.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponseDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;
    /** Só aparece em erro 400 de validação: campo -> mensagem. */
    private final Map<String, String> campos;

    public ErroResponseDTO(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public String getCaminho() { return caminho; }
    public Map<String, String> getCampos() { return campos; }
}
