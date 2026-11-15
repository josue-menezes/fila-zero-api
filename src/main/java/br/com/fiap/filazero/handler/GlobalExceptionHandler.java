package br.com.fiap.filazero.handler;

import br.com.fiap.filazero.dto.ErroResponseDTO;
import br.com.fiap.filazero.exception.BancoDeDadosException;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.RegraDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler global: transforma TODA exceção num JSON padronizado (ErroResponseDTO),
 * com o status HTTP certo e sem stack trace nem SQL.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404: registro buscado pelo código não existe. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    /** 409: qualquer regra de negócio violada (RN01 a RN07, duplicidade). */
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponseDTO> regraDeNegocio(RegraDeNegocioException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    /** 400: Bean Validation dos DTOs (@NotBlank, @Size, @Future...), com a lista de campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos. Verifique os campos informados.", req, campos);
    }

    /** 400: dados coerentes individualmente, mas inválidos juntos. */
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponseDTO> dadosInvalidos(DadosInvalidosException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    /** 400: JSON mal formado, data em formato errado ou status inexistente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido. Confira o JSON, as datas (yyyy-MM-ddTHH:mm:ss) e os valores.", req, null);
    }

    /** 400: parâmetro de URL com tipo errado (ex.: /unidades/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.", req, null);
    }

    /** 400: parâmetro obrigatório ausente (ex.: ?data= em /horarios). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> parametroAusente(MissingServletRequestParameterException e,
                                                            HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + e.getParameterName() + "' é obrigatório.", req, null);
    }

    /** 404: rota que não existe. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada.", req, null);
    }

    /** 405: método HTTP não suportado na rota (ex.: DELETE /unidades). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> metodoNaoPermitido(HttpRequestMethodNotSupportedException e,
                                                              HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + e.getMethod() + " não é suportado nesta rota.", req, null);
    }

    /** 415: corpo enviado sem Content-Type JSON. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tipoDeConteudo(HttpMediaTypeNotSupportedException e,
                                                          HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo da requisição em JSON.", req, null);
    }

    /** 500: falha no banco. O detalhe vai só para o log do servidor, nunca para o cliente. */
    @ExceptionHandler(BancoDeDadosException.class)
    public ResponseEntity<ErroResponseDTO> banco(BancoDeDadosException e, HttpServletRequest req) {
        LOG.error("Erro de banco de dados em {}", req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível acessar os dados agora. Tente novamente em instantes.", req, null);
    }

    /** 500: qualquer erro inesperado, com mensagem genérica. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> inesperado(Exception e, HttpServletRequest req) {
        LOG.error("Erro inesperado em {}", req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente.", req, null);
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String mensagem,
                                                     HttpServletRequest req, Map<String, String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(status.value(), status.getReasonPhrase(), mensagem,
                req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
