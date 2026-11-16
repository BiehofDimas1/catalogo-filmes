# agent.md

Documentação do projeto para agentes de IA e para a equipe. Leia antes de mudar código, contratos HTTP ou regras de negócio. O `README.md` apresenta o sistema para quem avalia; este arquivo registra o contexto, as decisões e as convenções para quem vai trabalhar no código.

## Contexto

Trabalho acadêmico de Programação IV, atividade "Projetos baseados no Mini Spring", tema **Catálogo de filmes** (Grupo 5): filmes de uma plataforma de streaming, com aluguel. O projeto de referência é [ferreiralisson/ingresso](https://github.com/ferreiralisson/ingresso); este repositório adapta o fluxo de pedidos dele para o aluguel de filmes.

São dois projetos independentes, sem módulo agregador:

| Pasta | Tecnologia | Papel |
|---|---|---|
| `filmes-api/` | Java 21, Spring Boot 4.1, Spring Data JPA, Bean Validation, Spring Security + JWT (`java-jwt`), H2 em arquivo | API REST na porta `8080` |
| `filmes-web/` | Angular 22 (standalone, signals, zoneless), TypeScript 6, RxJS, Vitest | Telas na porta `4200` |

O Angular chama a API pelo endereço definido em `filmes-web/src/app/core/api.ts`. A API libera CORS para `http://localhost:4200` (`app.cors.origens`).

## Requisitos da atividade

1. Backend em Java com Spring Boot, frontend em Angular, API REST, JPA e camadas controller → service → repository.
2. Cadastro, login com JWT e controle de acesso por perfil: cada usuário só acessa os dados e as operações autorizados.
3. Entidades, relacionamentos, endpoints e telas do tema, com cadastro, consulta, atualização e inativação.
4. Fluxo principal equivalente ao de pedidos do projeto-base, com controle de disponibilidade, status e histórico do usuário.
5. Regras do tema: duplicidade, limites de quantidade, disponibilidade e cancelamento; pagamento e reembolso simulados.
6. Telas integradas à API com mensagens de sucesso, validação e erro; testes das regras e de acesso não autorizado.

Além disso, a adaptação precisa fazer sentido no tema. QR Code e validação de entrada devem ser adaptados quando houver equivalente; senão, a não utilização deve ser justificada no README.

Exigências do tema (planilha de temas): entidade `Filme` com `titulo` (texto) e `duracaoMinutos` (número), ID gerado automaticamente, rota base `/filmes` e no máximo 600 minutos.

## Diagnóstico inicial

Estado encontrado em 14/11/2026 no repositório `BiehofDimas1/catalogo-filmes` e o que foi feito:

| Situação encontrada | Correção |
|---|---|
| O `main` só tinha o scaffold do Spring Boot; o trabalho da equipe estava em `feature/filme-crud` | O trabalho continuou no branch `feature/auth-aluguel`, criado a partir dele |
| `spring-boot-starter-security` sem configuração fazia o Spring exigir login em todas as rotas | `SecurityConfig` com JWT, CORS e regras por perfil |
| Login e cadastro eram só telas visuais | Integrados à API, com sessão, interceptor e guards |
| Não existiam `Usuario`, `Aluguel`, JWT nem as regras do aluguel | Implementados (requisitos 2, 4 e 5) |
| A API expunha a entidade JPA `Filme` e não tinha tratamento global de erros | DTOs e `ApiExceptionHandler` com ProblemDetail em português |
| Títulos de filme podiam se repetir | Título único, sem diferenciar maiúsculas |
| O README descrevia funcionalidades que não existiam | README reescrito a partir do código |
| Testes do Angular quebrados: título inexistente, interface injetada como serviço, providers ausentes | Specs reescritos e ampliados |
| URL da API fixa dentro do serviço de filmes | Centralizada em `core/api.ts` |

### Revisão depois do merge (16/11/2026)

O PR #1 levou o trabalho para o `main`. A revisão do `main` contra os requisitos, feita num clone limpo, encontrou dois pontos, corrigidos no branch `fix/versao-edicao-filme`:

| Situação encontrada | Correção |
|---|---|
| O formulário de edição gravava as licenças que estavam na tela ao abri-lo; um aluguel, cancelamento ou expiração nesse meio-tempo era desfeito, e o filme ganhava ou perdia uma licença | `PUT /filmes/{id}` exige a `versao` carregada e responde 409 se o filme mudou; o formulário envia a versão e mostra o aviso |
| Este arquivo ainda dizia que o trabalho estava em `feature/auth-aluguel` | Seção "Integração e entrega" atualizada |

## Decisões tomadas no grill-me

Entrevista feita com a skill grill-me em 14/11/2026, uma pergunta por vez, antes de implementar:

| # | Pergunta | Decisão | Motivo |
|---|---|---|---|
| Q1 | `Usuario` e `Aluguel` podem existir além de `Filme`, apesar da regra "uma entidade por grupo"? | Sim. `Filme` é a entidade do tema; as outras são de apoio | Login e fluxo de pedidos não existem sem registros próprios |
| Q2 | O fluxo principal é o aluguel de 48 horas descrito no README da equipe? | Sim | As licenças viram disponibilidade, a validade vira status e o cancelamento vira reembolso |
| Q3 | Visitantes sem login veem o catálogo? | Não; só cadastro e login | Acesso restrito deixa claros os testes de acesso não autorizado |
| Q4 | Os perfis são só ADMIN e CLIENTE, com as permissões da tabela do README? | Sim | Separa a gestão do catálogo da locação |
| Q5 | Como o primeiro ADMIN é criado? | Manualmente, pelo console do H2 | Escolha da equipe |
| Q6 | O H2 fica em memória ou em arquivo? | Em arquivo | O ADMIN criado no console precisa sobreviver aos reinícios |
| Q7 | As regras de aluguel do README valem como estão? | Sim: 5 ativos por usuário, 1 por filme, cancelamento em até 1 hora, expiração em 48 horas | Cobrem os limites e o cancelamento do requisito 5 |
| Q8 | O preço é fixo ou por filme? | Fixo: R$ 9,90, configurável | Mantém o `Filme` com os campos do tema |
| Q9 | O pagamento simulado sempre aprova? | Aprova por padrão; a recusa só é configurada no servidor; não há estado pendente | Permite testar o erro sem criar um estado que o README não define |
| Q10 | O que é um filme duplicado? | Mesmo título, sem diferenciar maiúsculas, inclusive se o outro estiver inativo | Regra simples de validar; o custo é bloquear remakes com o mesmo nome |
| Q11 | O QR Code vira um código de acesso validado por endpoint, sem imagem? | Sim, validado pelo ADMIN | É o equivalente da validação de entrada |
| Q12 | Inativar um filme cancela os aluguéis ativos? | Não; eles valem até expirar | O cliente já pagou |
| Q13 | O ADMIN pode reativar um filme inativo? | Sim | Sem isso, o título inativo ficaria bloqueado para sempre |
| Q14 | Validar o código consome o acesso? | Não | O filme pode ser assistido mais de uma vez dentro das 48 horas |

Premissas adotadas sem objeção da equipe: o grupo é o 5; o cadastro público sempre cria CLIENTE; só o dono cancela o próprio aluguel, com reembolso integral; o e-mail de usuário é único.

Não mude essas decisões sem combinar de novo com a equipe.

## Regras de negócio em vigor

- **Perfis:** `ADMIN` e `CLIENTE`. O perfil nunca vem do cliente. O primeiro `ADMIN` é promovido pelo console do H2 (passo a passo no README).
- **Acesso:** visitantes só usam cadastro e login. `ADMIN` gerencia filmes, vê todos os aluguéis e valida códigos de acesso. `CLIENTE` vê os filmes ativos, aluga, cancela os próprios aluguéis e vê o próprio histórico.
- **Filme:** título obrigatório e único sem diferenciar maiúsculas; duração de 1 a 600 minutos; licenças disponíveis maiores ou iguais a zero. `DELETE /filmes/{id}` inativa; não há exclusão física.
- **Edição de filme:** o `PUT /filmes/{id}` exige a `versao` que o formulário carregou (400 sem ela). Se o filme mudou desde então, inclusive por aluguel, cancelamento ou expiração, a resposta é 409 e nada é gravado, para não desfazer a contagem de licenças.
- **Aluguel:** só de filme ativo com licença disponível; cobra o pagamento simulado, vale 48 horas e consome uma licença. No máximo 5 aluguéis ativos por usuário e 1 ativo por filme.
- **Status:** `ATIVO` vira `EXPIRADO` depois de 48 horas, devolvendo a licença, ou `CANCELADO` quando o dono cancela em até 1 hora, com reembolso integral simulado e devolução da licença.
- **Código de acesso:** UUID único por aluguel, mostrado ao dono enquanto o aluguel está ativo. O ADMIN valida em `GET /alugueis/acesso/{codigo}`, sem consumir o código.

Os valores das regras ficam em `filmes-api/src/main/resources/application.properties` (`aluguel.*`). Ao mudar uma regra, atualize os testes, o README e este arquivo.

## Arquitetura

### API (`filmes-api`, pacote `com.catalogo.filmes`)

| Pacote | Conteúdo |
|---|---|
| `config` | `SecurityConfig` (rotas por perfil, CORS, BCrypt), `ApiExceptionHandler`, propriedades `aluguel.*`, `jwt.*` e `pagamento.simulado.*`, relógio da aplicação |
| `controller` | `AuthController` (`/auth`), `FilmeController` (`/filmes`), `AluguelController` (`/alugueis`) |
| `dto` | Records de entrada e saída, com Bean Validation e mensagens em português |
| `model` | Entidades JPA `Filme`, `Usuario` e `Aluguel`; enums `Perfil` e `StatusAluguel` |
| `repository` | Repositórios Spring Data JPA |
| `security` | `TokenService` (JWT), `SecurityFilter`, `UsuarioLogado`, `UsuarioDetailsService` e as respostas 401/403 |
| `service` | Regras: `FilmeService`, `AuthService`, `AluguelService` e a rotina `AluguelExpiracaoJob` |
| `service.exception` | Exceções de negócio, traduzidas em status HTTP |
| `service.pagamento` | `PagamentoGateway` e a implementação simulada |

Relacionamentos: `Usuario` 1 — N `Aluguel` N — 1 `Filme`.

Autenticação: `POST /auth/login` devolve o JWT (emissor `filmes-api`, assunto igual ao e-mail, validade de 2 horas). A cada requisição, o `SecurityFilter` valida o token e recarrega o usuário e o perfil do banco; uma promoção a ADMIN vale na hora para a API.

### Web (`filmes-web/src/app`)

| Caminho | Conteúdo |
|---|---|
| `core/api.ts` | Endereço da API |
| `core/erro-http.ts` | Monta a mensagem de erro a partir do ProblemDetail |
| `core/auth/` | `AuthService` (sessão no `sessionStorage`), interceptor JWT e guards por perfil |
| `filme.ts`, `aluguel.ts` | Serviços HTTP e tipos de cada domínio |
| `filme-lista/`, `filme-form/` | Catálogo (aluguel para o CLIENTE, gestão para o ADMIN) e formulário de filme |
| `login/`, `cadastro/` | Entrada e criação de conta |
| `meus-alugueis/` | Histórico e cancelamento do CLIENTE |
| `alugueis-admin/` | Todos os aluguéis e validação de código (ADMIN) |

Rotas: `/login` e `/cadastro` (só visitante), `/filmes` (logado), `/filmes/novo` e `/filmes/:id/editar` (ADMIN), `/meus-alugueis` (CLIENTE), `/admin/alugueis` (ADMIN).

## Endpoints

| Método | Rota | Perfil |
|---|---|---|
| POST | `/auth/cadastro`, `/auth/login` | público |
| GET | `/auth/me`, `/filmes`, `/filmes/{id}`, `/alugueis/regras` | logado |
| POST, PUT, DELETE, PATCH | `/filmes`, `/filmes/{id}`, `/filmes/{id}/reativar` | ADMIN |
| POST | `/alugueis`, `/alugueis/{id}/cancelar` | CLIENTE (cancelar só o próprio) |
| GET | `/alugueis/meus` | CLIENTE |
| GET | `/alugueis`, `/alugueis/acesso/{codigo}` | ADMIN |

A descrição de cada endpoint e dos códigos de erro está no README.

## Comandos

```sh
# API (no Windows, use mvnw.cmd)
cd filmes-api
./mvnw test
./mvnw spring-boot:run

# Web
cd filmes-web
npm ci
npm test -- --watch=false
npm run build
npm start
```

- O `pom.xml` mira Java 21. Com JDK 17 dá para compilar e testar passando `-Djava.version=17` (no PowerShell, entre aspas: `"-Djava.version=17"`), desde que o código não use recursos do Java 18 em diante.
- O Angular CLI 22.2 exige Node 22.22.3 ou mais recente (ou 24.15+). Com Node mais antigo, `ng build`, `ng test` e o `ngc` não rodam; o `tsc` ainda consegue checar os tipos do TypeScript, sem os templates.
- `JWT_SECRET` é opcional e precisa ter pelo menos 32 bytes. Sem ele, a API gera uma chave aleatória a cada início e os tokens deixam de valer quando ela reinicia.
- `PAGAMENTO_SIMULADO=RECUSADO` faz o pagamento simulado recusar, para demonstrar o erro.
- O banco H2 fica em `filmes-api/data/`, fora do git. Para começar do zero, pare a API e apague essa pasta.

## Convenções da API

- Siga `controller` → `service` → `repository`. Controllers só adaptam HTTP, services concentram as regras e repositories só acessam dados.
- Use DTOs (records em `dto/`) nas fronteiras HTTP; nunca exponha entidades JPA. Valide a entrada com Bean Validation nos DTOs, com mensagens em português.
- Erros de negócio usam as exceções de `service/exception` e são traduzidos por `config/ApiExceptionHandler` em ProblemDetail (`title`, `status`, `detail` e, em erros de validação, `erros` por campo). Status usados: 400 validação, 401 sem login ou credenciais inválidas, 403 perfil ou dono errado, 404 não encontrado, 409 duplicidade ou conflito de concorrência, 422 regra de negócio.
- A autorização é do servidor: regras por rota em `config/SecurityConfig` e verificação de dono nos services.
- Senhas são gravadas com BCrypt. Não registre senhas, tokens ou segredos em log e não coloque segredos ou credenciais padrão no código.
- Concorrência: `Filme`, `Aluguel` e `Usuario` têm `@Version`, e conflitos viram 409 com pedido para tentar de novo. A edição de filme compara a `versao` enviada com a atual (`VersaoDesatualizadaException`, 409), porque o `@Version` sozinho não percebe mudanças feitas antes de a requisição chegar; `FilmeResponse` expõe a `versao` para isso. A expiração roda a cada minuto e também é aplicada ao filme no momento do aluguel. As respostas sempre mostram o status efetivo.
- Pagamento e reembolso passam por `service/pagamento/PagamentoGateway`; a implementação atual é simulada e o cliente nunca escolhe o resultado.

## Convenções do web

- Componentes standalone, formulários reativos e signals. O app é zoneless (não usa `zone.js`): todo estado alterado de forma assíncrona deve ficar em signals.
- Serviços HTTP por domínio (`filme.ts`, `aluguel.ts`). Não espalhe URLs pelos componentes.
- O interceptor só envia `Authorization: Bearer` para a API e nunca para login e cadastro. Em resposta 401, encerra a sessão e volta ao login.
- Guards e o perfil guardado na sessão servem só à navegação; quem autoriza é a API. Não chame `AuthService.token()` nem `estaLogado()` em templates (eles podem encerrar a sessão); use os signals `usuario`, `ehAdmin` e `ehCliente`.
- Textos de interface em português. Toda chamada à API mostra mensagem de sucesso, validação ou erro, usando `mensagemDeErro`.

## Testes

- **API** (`filmes-api/src/test`): testes unitários de modelo e serviço (Mockito, com relógio fixo para os prazos), validação dos DTOs, JWT e testes de API com MockMvc. `SegurancaApiTest` cobre os acessos 401/403 e `AluguelFluxoApiTest` cobre o fluxo completo. O perfil `test` usa H2 em memória.
- **Web** (`*.spec.ts`): Vitest com `HttpTestingController`. Componentes que usam HTTP ou rotas precisam de `provideHttpClient()`, `provideHttpClientTesting()` e `provideRouter([])`. Quando o componente chama a API no `ngOnInit`, use `fixture.detectChanges()`, responda as requisições e só então `await fixture.whenStable()`.
- **Estado verificado em 16/11/2026:** 74 testes da API e 57 do Angular passando, build de produção e checagem estrita de templates sem erros. O fluxo completo no navegador (cadastro, promoção a ADMIN pelo console do H2, filme, aluguel, validação do código e cancelamento) foi testado antes da checagem de versão na edição; esse caso é coberto por `AluguelFluxoApiTest` e pelos testes do formulário.

## Limitações conhecidas

- O H2 em arquivo é para uso local; não é um banco de produção.
- Pagamento, reembolso e entrega do código são simulados. O código de acesso fica em texto no banco.
- Sem `JWT_SECRET`, os tokens deixam de valer a cada reinício da API.
- Remakes com o mesmo título são bloqueados (decisão Q10).

## Integração e entrega

- Mudou endpoint, payload ou regra? Atualize a API, as telas, os testes dos dois lados, o README e este arquivo na mesma mudança.
- O código está no `main` desde o PR #1 (16/11/2026). Para mudar algo, crie um branch a partir do `main` e abra um pull request; não faça push direto no `main` nem merge sem combinar com a equipe.
