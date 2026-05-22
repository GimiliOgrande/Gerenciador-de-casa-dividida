# House Manager 1.0

Aplicacao web para gerenciar casas alugadas compartilhadas. A ideia principal e ajudar moradores a controlar aluguel, contas, pagamentos, tarefas domesticas e responsabilidades da casa em um unico lugar.

## Estrutura Do Projeto

O repositorio esta dividido em dois projetos principais:

```text
.
+-- backend/
|   +-- house.manager/          # API Spring Boot
+-- frontend/
|   +-- front-house-manager/    # Aplicacao React com Vite
+-- docker-compose.yml          # PostgreSQL local
+-- AGENTS.md                   # Regras de arquitetura e padroes do projeto
+-- README.md
```

## Arquitetura Backend

O backend usa Spring Boot e preserva a arquitetura em camadas:

```text
Controller -> Service -> Repository -> Model
```

Responsabilidades principais:

- `controller`: recebe requisicoes HTTP, aplica rotas, status codes e DTOs.
- `service`: concentra regras de negocio, validacoes, permissoes e transacoes.
- `repository`: acesso ao banco de dados via Spring Data JPA.
- `model`: entidades persistidas no banco.
- `dto`: contratos de entrada e saida da API.
- `security`: autenticacao, filtro JWT e utilitarios de seguranca.
- `exception`: tratamento padronizado de erros da API.
- `config`: configuracoes do Spring, como seguranca e CORS.

Regras importantes:

- Controllers nao devem acessar repositories diretamente.
- Validacoes de permissao devem ficar nos services sempre que possivel.
- Um usuario so pode acessar dados de casas das quais ele e membro.
- Algumas acoes, como adicionar moradores, devem ser restritas ao dono da casa.

## Autenticacao

A autenticacao usa JWT armazenado em cookie HTTP-only chamado `token`.

Fluxo atual:

1. Usuario faz login ou cadastro.
2. Backend gera um JWT usando `JwtUtil`.
3. Token e enviado em um cookie `token`.
4. As proximas requisicoes usam esse cookie automaticamente.
5. O filtro `JwtAuthenticationFilter` valida o cookie e popula o usuario autenticado.

Endpoints de autenticacao:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

## Banco De Dados

O banco usado e PostgreSQL.

Configuracao padrao do backend:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/house_manager
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Esses valores podem ser sobrescritos por variaveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JPA_DDL_AUTO
```

Para desenvolvimento local, existe um `docker-compose.yml` na raiz com PostgreSQL pronto para uso.

## Dominio Inicial

O MVP atual possui as seguintes entidades principais:

- `User`: usuario da aplicacao.
- `House`: casa compartilhada.
- `HouseMember`: relacionamento entre usuario e casa.
- `Expense`: despesa da casa, como aluguel, energia ou internet.
- `ExpensePayment`: pagamento individual de cada morador em uma despesa.
- `HouseTask`: tarefa domestica da casa.

Tambem existem enums para regras de estado e permissao:

- `MembershipRole`: `OWNER` ou `MEMBER`.
- `PaymentStatus`: `PENDING` ou `PAID`.
- `TaskStatus`: `PENDING` ou `DONE`.

## Endpoints Principais

Casas e moradores:

```text
GET  /api/houses
POST /api/houses
GET  /api/houses/{houseId}/members
POST /api/houses/{houseId}/members
GET  /api/houses/{houseId}/dashboard
```

Despesas:

```text
GET  /api/houses/{houseId}/expenses
POST /api/houses/{houseId}/expenses
POST /api/expenses/{expenseId}/payments/me/paid
```

Tarefas:

```text
GET  /api/houses/{houseId}/tasks
POST /api/houses/{houseId}/tasks
POST /api/tasks/{taskId}/done
```

## Arquitetura Frontend

O frontend usa React com Vite.

Estrutura principal:

```text
frontend/front-house-manager/
├── src/
│   ├── App.jsx
│   ├── App.css
│   ├── index.css
│   └── services/
│       └── api.js
```

O arquivo `src/services/api.js` centraliza as chamadas HTTP para o backend. As requisicoes usam `credentials: 'include'` para enviar o cookie `token`.

A interface atual possui:

- login e cadastro;
- criacao e selecao de casas;
- dashboard da casa;
- listagem de moradores;
- cadastro de despesas;
- marcacao de pagamento como pago;
- cadastro de tarefas;
- marcacao de tarefa como concluida.

## Como Rodar Localmente

### 1. Subir o PostgreSQL

Na raiz do projeto:

```powershell
docker compose up -d
```

Isso sobe um PostgreSQL local com:

```text
database: house_manager
user: postgres
password: postgres
port: 5432
```

### 2. Rodar o Backend

```powershell
cd backend\house.manager
.\gradlew.bat bootRun
```

A API ficara disponivel em:

```text
http://localhost:8081/api
```

### 3. Rodar o Frontend

Em outro terminal:

```powershell
cd frontend\front-house-manager
npm.cmd install
npm.cmd run dev
```

O frontend ficara disponivel em:

```text
http://localhost:5173
```

Por padrao, o frontend chama a API em:

```text
http://localhost:8081/api
```

Para alterar a URL da API, configure:

```text
VITE_API_BASE_URL
```

Exemplo:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm.cmd run dev
```

## Comandos Uteis

Backend:

```powershell
cd backend\house.manager
.\gradlew.bat test --no-daemon
.\gradlew.bat build -x test --no-daemon
```

Frontend:

```powershell
cd frontend\front-house-manager
npm.cmd run lint
npm.cmd run build
```

Docker:

```powershell
docker compose up -d
docker compose down
```

## Padroes Do Projeto

- Codigo, classes, variaveis, metodos, componentes e campos de API devem usar nomes em ingles.
- Use `camelCase` para variaveis, funcoes, metodos e propriedades.
- Use `PascalCase` para classes Java, componentes React e tipos/DTOs.
- Textos visiveis para o usuario podem ficar em portugues.
- Evite expor entidades JPA diretamente quando houver relacionamentos, dados sensiveis ou detalhes internos.
- Mantenha regras de negocio e permissao nos services.
- Evite refatoracoes fora do escopo da feature atual.

## Proximos Passos Sugeridos

- Criar migrations com Flyway ou Liquibase.
- Adicionar testes de service para permissoes e regras financeiras.
- Melhorar roles dentro da casa, como dono/admin/morador.
- Criar convites para novos moradores.
- Adicionar recorrencia para despesas e tarefas.
- Criar historico mensal de pagamentos.
- Adicionar notificacoes de vencimento.
- Melhorar tratamento visual de erros no frontend.
