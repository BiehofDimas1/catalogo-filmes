# filmes-web

Frontend Angular do Catálogo de Filmes. Consome a API `filmes-api` em `http://localhost:8080`; o endereço fica em `src/app/core/api.ts`.

Requisito: **Node.js 22.22.3 ou mais recente** (ou 24.15+), exigência do Angular CLI 22.2.

## Rodar em desenvolvimento

```bash
npm install
npm start
```

Abra `http://localhost:4200`. A API precisa estar rodando (veja o README da raiz).

## Testes

```bash
npm test -- --watch=false
```

Os testes usam Vitest e cobrem a sessão e o interceptor JWT, os guards por perfil, as mensagens de erro da API e todas as telas.

## Build de produção

```bash
npm run build
```

A saída fica em `dist/filmes-web`.

## Organização

| Caminho | Conteúdo |
|---|---|
| `src/app/core/` | Endereço da API, mensagens de erro, sessão, interceptor e guards |
| `src/app/filme.ts`, `src/app/aluguel.ts` | Serviços HTTP e tipos |
| `src/app/<tela>/` | Uma pasta por tela: catálogo, formulário de filme, login, cadastro, meus aluguéis e aluguéis do ADMIN |

As regras de negócio e as convenções do projeto estão no `README.md` e no `agent.md` da raiz.
