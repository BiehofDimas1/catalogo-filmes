package com.catalogo.filmes.config;

import com.catalogo.filmes.service.exception.AcessoNegadoException;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.CredenciaisInvalidasException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.exception.VersaoDesatualizadaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduz as exceções em respostas ProblemDetail com mensagens em português.
 * Campos: {@code title}, {@code status}, {@code detail} e, em erros de validação, {@code erros} por campo.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final HttpStatusCode REGRA_DE_NEGOCIO = HttpStatusCode.valueOf(422);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Verifique os campos informados.");
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O corpo da requisição está vazio ou mal formatado."));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Um parâmetro da requisição tem formato inválido."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> tratarViolacao(ConstraintViolationException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacao : ex.getConstraintViolations()) {
            String caminho = violacao.getPropertyPath().toString();
            String campo = caminho.substring(caminho.lastIndexOf('.') + 1);
            erros.putIfAbsent(campo, violacao.getMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Verifique os campos informados.");
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, "Registro duplicado", ex.getMessage());
    }

    @ExceptionHandler(VersaoDesatualizadaException.class)
    public ResponseEntity<ProblemDetail> tratarVersaoDesatualizada(VersaoDesatualizadaException ex) {
        return resposta(HttpStatus.CONFLICT, "Conflito de atualização", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> tratarRegra(RegraNegocioException ex) {
        return resposta(REGRA_DE_NEGOCIO, "Operação não permitida", ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegado(AcessoNegadoException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegadoSpring(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado",
                "O seu perfil não tem permissão para esta operação.");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ProblemDetail> tratarCredenciais(CredenciaisInvalidasException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> tratarConcorrencia(OptimisticLockingFailureException ex) {
        return resposta(HttpStatus.CONFLICT, "Conflito de atualização",
                "Outra operação alterou estes dados ao mesmo tempo. Tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Registro duplicado",
                "A operação conflita com dados já cadastrados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarInesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private static ResponseEntity<ProblemDetail> resposta(HttpStatusCode status, String titulo, String detalhe) {
        return ResponseEntity.status(status).body(problema(status, titulo, detalhe));
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
