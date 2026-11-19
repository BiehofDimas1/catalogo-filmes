package com.catalogo.filmes.config;

import com.catalogo.filmes.service.exception.ErroDaAplicacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(problemaDeValidacao(erros));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return requisicaoInvalida("O corpo da requisição está vazio ou mal formatado.");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        return requisicaoInvalida("Um parâmetro da requisição tem formato inválido.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> tratarViolacao(ConstraintViolationException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacao : ex.getConstraintViolations()) {
            erros.putIfAbsent(nomeDoCampo(violacao), violacao.getMessage());
        }
        return ResponseEntity.badRequest().body(problemaDeValidacao(erros));
    }

    @ExceptionHandler(ErroDaAplicacao.class)
    public ResponseEntity<ProblemDetail> tratarErroDaAplicacao(ErroDaAplicacao ex) {
        return resposta(HttpStatusCode.valueOf(ex.status()), ex.titulo(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegadoSpring(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado",
                "O seu perfil não tem permissão para esta operação.");
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

    private static String nomeDoCampo(ConstraintViolation<?> violacao) {
        String caminho = violacao.getPropertyPath().toString();
        return caminho.substring(caminho.lastIndexOf('.') + 1);
    }

    private static ResponseEntity<Object> requisicaoInvalida(String detalhe) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Requisição inválida", detalhe);
        return ResponseEntity.badRequest().body(problema);
    }

    private static ProblemDetail problemaDeValidacao(Map<String, String> erros) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Verifique os campos informados.");
        problema.setProperty("erros", erros);
        return problema;
    }

    private static ResponseEntity<ProblemDetail> resposta(HttpStatusCode status, String titulo,
                                                          String detalhe) {
        return ResponseEntity.status(status).body(problema(status, titulo, detalhe));
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
