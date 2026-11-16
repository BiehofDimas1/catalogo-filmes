# Catálogo de Filmes

Sistema web de catálogo de filmes de uma plataforma de streaming, com aluguel de filmes por 48 horas e pagamento simulado.

Projeto acadêmico de Programação IV (atividade "Projetos baseados no Mini Spring", **Grupo 5, tema 2: Catálogo de filmes**, rota base `/filmes`, máximo de 600 minutos). Segue a organização do projeto de referência [Ingresso](https://github.com/ferreiralisson/ingresso): camadas controller / service / repository, JPA, JWT e perfis de acesso. O fluxo de pedidos do Ingresso virou o **aluguel de filmes**.

## Tecnologias

- **Backend (`filmes-api`):** Java 21, Spring Boot 4.1, Spring Data JPA, Bean Validation, Spring Security + JWT (`java-jwt`), banco H2 em arquivo
- **Frontend (`filmes-web`):** Angular 22 (componentes standalone, signals, formulários reativos)
- **Testes:** JUnit 5, Mockito e MockMvc na API; Vitest no Angular

## Equipe

- **Biehof:** CRUD de filmes (backend e telas), telas de login e cadastro, mensagens de sucesso/erro, testes de filmes, README e apresentação
- **Marcus:** autenticação JWT, segurança por perfil, fluxo principal de aluguel e suas regras, tratamento global de erros

## Entidades

A planilha de temas pede uma entidade por grupo: aqui ela é o **Filme**. `Usuario` e `Aluguel` são entidades de apoio, exigidas pelos requisitos de login (2) e de fluxo principal (4).

| Entidade | Campos | Relacionamentos |
|---|---|---|
| **Filme** (tema) | `id` gerado automaticamente, `titulo`, `duracaoMinutos` (1 a 600), `licencasDisponiveis`, `ativo` | um filme tem muitos aluguéis |
| **Usuario** | `id`, `nome`, `email` (único), `senha` (BCrypt), `perfil` (`ADMIN` ou `CLIENTE`) | um usuário tem muitos aluguéis |
| **Aluguel** | `id`, usuário, filme, data do aluguel, data de expiração, data de cancelamento, `status` (`ATIVO`, `EXPIRADO`, `CANCELADO`), `valor`, transação de pagamento e de reembolso simulados, código de acesso | pertence a um usuário e a um filme |

## Perfis de acesso

| Perfil | Pode fazer |
|---|---|
| Visitante (sem login) | Criar conta e entrar. Não vê o catálogo. |
| CLIENTE | Ver os filmes ativos, alugar, cancelar o próprio aluguel dentro do prazo e ver o próprio histórico |
| ADMIN | Cadastrar, editar, inativar e reativar filmes; ver todos os aluguéis; validar códigos de acesso |

O cadastro público sempre cria `CLIENTE`, mesmo que alguém envie outro perfil. A autorização é feita na API: as telas só escondem o que o perfil não pode usar.

## Fluxo principal: aluguel de filme

1. O cliente logado escolhe um filme ativo no catálogo.
2. A API confere as regras abaixo, cobra o **pagamento simulado** (R$ 9,90) e registra o aluguel, válido por **48 horas**.
3. O aluguel consome uma licença do filme e recebe um **código de acesso**.
4. O cliente acompanha o status em **Meus aluguéis**:
   - `ATIVO`: dentro da validade;
   - `EXPIRADO`: passaram 48 horas, e a licença volta para o filme;
   - `CANCELADO`: o cliente cancelou em até 1 hora, recebeu o **reembolso simulado** integral e a licença voltou para o filme.

Uma rotina da API expira os aluguéis vencidos a cada minuto. Antes de checar a disponibilidade de um filme, a API também devolve as licenças de aluguéis que já venceram.

## Regras de negócio

- A duração do filme vai de **1 a 600 minutos**.
- Não pode haver dois filmes com o **mesmo título**, sem diferenciar maiúsculas, inclusive entre filmes inativos.
- O cliente não pode ter dois aluguéis **ativos** do mesmo filme.
- O cliente pode ter no máximo **5 aluguéis ativos**.
- Só é possível alugar filme **ativo** e com **licenças disponíveis**.
- O aluguel só pode ser cancelado pelo próprio cliente, em até **1 hora**, com reembolso simulado.
- Não há exclusão física de filmes: `DELETE /filmes/{id}` **inativa** o filme e o ADMIN pode reativá-lo. Aluguéis ativos de um filme inativado continuam valendo até expirar; só novos aluguéis ficam bloqueados.
- A edição de um filme envia a `versao` que o formulário carregou. Se o filme mudou nesse meio-tempo, por exemplo porque um cliente alugou, a API recusa a edição (409) para não desfazer a contagem de licenças. Basta recarregar o formulário e salvar de novo.

Os valores (preço, validade, prazo de cancelamento e limite) ficam em `filmes-api/src/main/resources/application.properties`, no grupo `aluguel.*`.

## Pagamento e reembolso simulados

Como no projeto de referência, o pagamento passa por uma interface (`PagamentoGateway`) com implementação simulada. Nenhum dinheiro é movimentado. Por padrão o pagamento é aprovado; para demonstrar a recusa, inicie a API com `PAGAMENTO_SIMULADO=RECUSADO`. O cliente nunca escolhe o resultado do pagamento.

## QR Code e validação de entrada

No Ingresso, cada ingresso tem um QR Code que é validado na portaria. Aqui o equivalente é o **código de acesso** do aluguel:

- cada aluguel recebe um código único, exibido em **Meus aluguéis** enquanto o aluguel está ativo;
- o ADMIN valida o código na tela **Aluguéis** (`GET /alugueis/acesso/{codigo}`), que responde se o acesso está liberado (aluguel ativo) ou negado (expirado ou cancelado);
- validar **não consome** o código, porque um filme alugado pode ser assistido mais de uma vez dentro das 48 horas, ao contrário de um ingresso, que só entra uma vez.

Não geramos a **imagem** do QR Code porque o acesso ao streaming acontece na própria conta logada, sem leitura por câmera em uma portaria. O código em texto cumpre o mesmo papel de comprovar e validar o direito de acesso.

## Endpoints

| Método | Rota | Quem pode | Descrição |
|---|---|---|---|
| POST | `/auth/cadastro` | público | Cria conta de CLIENTE |
| POST | `/auth/login` | público | Devolve o token JWT |
| GET | `/auth/me` | logado | Dados do usuário logado |
| GET | `/filmes` | logado | ADMIN vê todos; CLIENTE vê só os ativos |
| GET | `/filmes/{id}` | logado | Busca um filme |
| POST | `/filmes` | ADMIN | Cadastra um filme |
| PUT | `/filmes/{id}` | ADMIN | Atualiza um filme; o corpo inclui a `versao` recebida no GET |
| DELETE | `/filmes/{id}` | ADMIN | Inativa um filme |
| PATCH | `/filmes/{id}/reativar` | ADMIN | Reativa um filme |
| GET | `/alugueis/regras` | logado | Preço, validade e limites atuais |
| POST | `/alugueis` | CLIENTE | Aluga um filme (`{ "filmeId": 1 }`) |
| GET | `/alugueis/meus` | CLIENTE | Histórico do cliente |
| POST | `/alugueis/{id}/cancelar` | CLIENTE (dono) | Cancela dentro do prazo |
| GET | `/alugueis` | ADMIN | Todos os aluguéis |
| GET | `/alugueis/acesso/{codigo}` | ADMIN | Valida um código de acesso |

Envie o token no cabeçalho `Authorization: Bearer <token>`. Os erros seguem o formato ProblemDetail, com mensagem em português no campo `detail` e, em erros de validação, os campos inválidos em `erros`:

| Status | Quando |
|---|---|
| 400 | Dados inválidos |
| 401 | Sem login, token inválido ou credenciais erradas |
| 403 | O perfil não permite a operação, ou o aluguel é de outro cliente |
| 404 | Registro não encontrado |
| 409 | Duplicidade (título, e-mail, aluguel ativo do mesmo filme), alteração simultânea ou edição de filme com versão desatualizada |
| 422 | Regra do aluguel (limite, sem licença, filme inativo, prazo de cancelamento, pagamento recusado) |

## Como executar

Requisitos: **Java 21** e **Node.js 22.22.3 ou mais recente** (ou 24.15+), versão exigida pelo Angular CLI 22.2.

**Backend** (porta 8080):

```
cd filmes-api
mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. Se só tiver o JDK 17, rode com `mvnw.cmd "-Djava.version=17" spring-boot:run`.

**Frontend** (porta 4200):

```
cd filmes-web
npm install
npm start
```

Acesse `http://localhost:4200`.

O banco H2 fica em arquivo, em `filmes-api/data/`: os dados continuam depois de reiniciar a API. Para começar do zero, pare a API e apague essa pasta.

### Variáveis de ambiente (opcionais)

| Variável | Para quê |
|---|---|
| `JWT_SECRET` | Segredo do JWT, com pelo menos 32 caracteres. Sem ela, a API gera um segredo aleatório a cada início e é preciso entrar de novo depois de reiniciar. |
| `PAGAMENTO_SIMULADO` | `APROVADO` (padrão) ou `RECUSADO`, para demonstrar a recusa do pagamento |
| `DB_URL` | Outro endereço para o banco H2 |

### Criar o primeiro ADMIN

O cadastro público só cria clientes. Para ter um administrador:

1. Com a API rodando, crie uma conta normal pela tela **Criar conta**.
2. Abra `http://localhost:8080/h2-console`.
3. Conecte com JDBC URL `jdbc:h2:file:./data/filmesdb`, usuário `sa` e senha em branco.
4. Execute, trocando o e-mail:

   ```sql
   UPDATE USUARIOS SET PERFIL = 'ADMIN' WHERE EMAIL = 'seu.email@exemplo.com';
   ```

5. No site, clique em **Sair** e entre de novo para ver o menu de administrador.

O console do H2 é só para uso local.

## Testes

```
cd filmes-api
mvnw test
```

Os testes da API cobrem as validações do filme (incluindo o limite de 600 minutos), a duplicidade de título e de e-mail, todas as regras do aluguel (limite de 5, um ativo por filme, disponibilidade, prazo de cancelamento, pagamento recusado, expiração), a edição de filme com versão desatualizada, o código de acesso, o JWT e os cenários de acesso não autorizado (401 sem login ou com token inválido, 403 para perfil errado e para aluguel de outro cliente).

```
cd filmes-web
npm test
```

Os testes do Angular cobrem a sessão e o interceptor JWT, os guards por perfil, a extração das mensagens de erro da API e as telas de login, cadastro, catálogo, formulário de filme, meus aluguéis e aluguéis do ADMIN.

## Documentação para agentes

O arquivo [`agent.md`](agent.md) reúne o diagnóstico inicial, as decisões tomadas com a equipe (grill-me), a arquitetura, as convenções e os comandos para quem for alterar o código, inclusive agentes de IA.
