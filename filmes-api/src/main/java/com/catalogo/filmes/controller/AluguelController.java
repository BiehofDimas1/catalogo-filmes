package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.AluguelRequest;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.dto.RegrasAluguelResponse;
import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.aluguel.AluguelService;
import com.catalogo.filmes.service.aluguel.CancelamentoAluguelService;
import com.catalogo.filmes.service.aluguel.ConsultaAluguelService;
import com.catalogo.filmes.service.aluguel.ValidacaoAcessoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
public class AluguelController {

    private final AluguelService aluguelService;
    private final CancelamentoAluguelService cancelamentoService;
    private final ConsultaAluguelService consultaService;
    private final ValidacaoAcessoService validacaoAcessoService;

    @GetMapping("/regras")
    public RegrasAluguelResponse regras() {
        return consultaService.regras();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AluguelResponse alugar(@Valid @RequestBody AluguelRequest dados,
                                  @AuthenticationPrincipal UsuarioLogado usuario) {
        return aluguelService.alugar(usuario.id(), dados.filmeId());
    }

    @GetMapping("/meus")
    public List<AluguelResponse> meus(@AuthenticationPrincipal UsuarioLogado usuario) {
        return consultaService.listarDoUsuario(usuario.id());
    }

    @PostMapping("/{id}/cancelar")
    public AluguelResponse cancelar(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UsuarioLogado usuario) {
        return cancelamentoService.cancelar(usuario.id(), id);
    }

    @GetMapping
    public List<AluguelResponse> todos() {
        return consultaService.listarTodos();
    }

    @GetMapping("/acesso/{codigo}")
    public ValidacaoAcessoResponse validarAcesso(@PathVariable("codigo") String codigo) {
        return validacaoAcessoService.validar(codigo);
    }
}
