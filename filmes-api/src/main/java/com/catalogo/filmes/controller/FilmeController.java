package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.filme.AtivacaoFilmeService;
import com.catalogo.filmes.service.filme.CadastroFilmeService;
import com.catalogo.filmes.service.filme.ConsultaFilmeService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/filmes")
@RequiredArgsConstructor
public class FilmeController {

    private final ConsultaFilmeService consultaService;
    private final CadastroFilmeService cadastroService;
    private final AtivacaoFilmeService ativacaoService;

    @GetMapping
    public List<FilmeResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario) {
        return consultaService.listar(usuario.isAdmin());
    }

    @GetMapping("/{id}")
    public FilmeResponse buscar(@PathVariable("id") Long id,
                                @AuthenticationPrincipal UsuarioLogado usuario) {
        return consultaService.buscar(id, usuario.isAdmin());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmeResponse cadastrar(@Valid @RequestBody FilmeRequest dados) {
        return cadastroService.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public FilmeResponse atualizar(@PathVariable("id") Long id,
                                   @Validated({Default.class, FilmeRequest.Edicao.class})
                                   @RequestBody FilmeRequest dados) {
        return cadastroService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable("id") Long id) {
        ativacaoService.inativar(id);
    }

    @PatchMapping("/{id}/reativar")
    public FilmeResponse reativar(@PathVariable("id") Long id) {
        return ativacaoService.reativar(id);
    }
}
