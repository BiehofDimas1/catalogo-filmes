# Catálogo de Filmes

Sistema web de catálogo de filmes de uma plataforma de streaming, com aluguel de filmes por 48 horas (pagamento simulado).

Projeto acadêmico em **Spring Boot + Angular**, seguindo a organização do projeto de referência "Ingresso" (camadas controller / service / repository, JPA, JWT e perfis de acesso).

## Tecnologias

- **Backend:** Java 21, Spring Boot, Spring Data JPA, Bean Validation, Spring Security + JWT, banco H2 (em memória)
- **Frontend:** Angular (componentes standalone, formulários reativos)
- **Testes:** JUnit 5 e Mockito

## Equipe

- **Biehof:** CRUD de filmes (backend e telas), telas de login e cadastro, mensagens de sucesso/erro, testes de filmes, README e apresentação
- **Marcus:** autenticação JWT, segurança por perfil, fluxo principal de aluguel e suas regras, tratamento global de erros

## Entidades

- **Usuario:** nome, email, senha, perfil (`ADMIN` ou `CLIENTE`)
- **Filme:** título, duração em minutos (1 a 600), ativo, licenças disponíveis
- **Aluguel:** usuário, filme, data do aluguel, data de expiração, status (`ATIVO`, `EXPIRADO`, `CANCELADO`), valor, pagamento simulado

## Perfis de acesso

| Perfil | Pode fazer |
|---|---|
| ADMIN | Cadastrar, editar e inativar filmes; ver todos os aluguéis |
| CLIENTE | Ver o catálogo, alugar filmes, cancelar aluguel e ver seu histórico |

## Fluxo principal: aluguel de filme

O cliente logado escolhe um filme ativo, o sistema registra um aluguel válido por **48 horas** com pagamento simulado e reduz em 1 as licenças disponíveis.

## Regras de negócio

- A duração do filme deve estar entre **1 e 600 minutos**.
- O usuário não pode ter dois aluguéis **ativos** do mesmo filme.
- O usuário pode ter no máximo **5 aluguéis ativos**.
- Só é possível alugar filme **ativo** e com **licenças disponíveis** (a licença é devolvida ao cancelar ou expirar).
- O aluguel só pode ser cancelado em até **1 hora**, com reembolso simulado.
- Filme com aluguéis ativos **não pode ser excluído**, apenas inativado.

## Endpoints de filmes

| Método | Rota | Descrição |
|---|---|---|
| GET | `/filmes` | Lista os filmes |
| GET | `/filmes/{id}` | Busca um filme |
| POST | `/filmes` | Cadastra um filme |
| PUT | `/filmes/{id}` | Atualiza um filme |
| DELETE | `/filmes/{id}` | Inativa um filme |

## Como executar

Requisitos: Java 21, Node.js e Angular CLI.

**Backend** (porta 8080):

```
cd filmes-api
mvnw spring-boot:run
```

**Frontend** (porta 4200):

```
cd filmes-web
npm install
ng serve
```

Acesse `http://localhost:4200`. O banco H2 é em memória: os dados são perdidos ao reiniciar o backend.

## Testes

```
cd filmes-api
mvnw test
```

Incluem a validação de duração (acima de 600 minutos deve falhar) e a listagem de filmes.

## QR Code

Não se aplica, pois o acesso ao filme é feito por conta logada, sem validação de entrada física. Como alternativa, cada aluguel poderia gerar um código de acesso (UUID).