# API de Lembretes

Esta é a API para gerenciamento de lembretes, desenvolvida com Spring Boot e Maven. A aplicação utiliza RabbitMQ para mensageria e PostgreSQL como banco de dados, ambos rodando via Docker no WSL.

## Tecnologias Utilizadas

- **Java** (Spring Boot)
- **Maven** (Gerenciamento de dependências e build)
- **RabbitMQ** (Mensageria para envio de lembretes)
- **PostgreSQL** (Banco de dados relacional)
- **Docker** (Para facilitar a execução dos serviços auxiliares)

## Requisitos

Antes de rodar a aplicação, certifique-se de ter instalado:

- **Java 17+**
- **Maven**
- **Docker e Docker Compose**
- **WSL2** (Para ambiente Windows)

## Configuração e Execução

### 1. Clonar o repositório

```sh
git clone https://github.com/seu-usuario/nome-do-repositorio.git
cd nome-do-repositorio
```

### 2. Subir os serviços Docker com o wsl já configurado

```sh
sudo service docker start
```

Iniciar o PostgreSQL e o RabbitMQ com ```docker run ${seu_container}```.


### 4. Rodar a aplicação

```sh
mvn spring-boot:run
```

## Endpoints Principais

### Criar um lembrete

```http
POST /api/v1/lembretes
```

#### Corpo da requisição (JSON):

```json
{
    "id": 1,
    "titulo": "Tarefa 1",
    "descricao": "Olá",
    "status": "CONCLUIDO",
    "categoriaId": 1,
    "recorrencia": {
      "quantidade": 0,
      "posicaoSemana": 0,
      "tipoRecorrencia": "DIARIO",
      "diasDaSemana": ["SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"],
      "intervaloCronExp": "0 0 0 */4* ? ?",
      "validade": "2025-08-03T00:00"
    },
    "datasEspecificas": ["2025-08-03T19:12"],
    "metodoNotificacao": ["som", "pop-up"]
}
```

### Listar lembretes

```http
GET /api/v1/lembretes
```

### Deletar um lembrete

```http
DELETE /api/v1/lembretes/{id}
```

## Segurança

Todos os endpoints exigem autenticação. Existem duas formas, habilitadas de forma global na
`SecurityConfig` — nenhum controller declara nada por endpoint:

| Forma | Como se identifica | Para quem |
|---|---|---|
| Sessão | cookie `JSESSIONID`, obtido no redirect do Google | navegador (tela inicial, Swagger UI) |
| Bearer | header `Authorization: Bearer <id_token>` | Postman, curl, app cliente |

O `id_token` do Google é validado pelo resource server contra o JWKS
(`https://www.googleapis.com/oauth2/v3/certs`), conferindo assinatura, expiração, emissor e audiência
(precisa ser o `GOOGLE_CLIENT_ID` desta aplicação — um token emitido para outro app é rejeitado).

O CSRF é ignorado apenas em `/api/**`: chamada de máquina se identifica pelo header, não por cookie de
formulário. É o que fazia `POST/PUT/DELETE` retornarem `403` no Postman e no Swagger.

### Perfil `dev` — testar fora do navegador

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

O perfil `dev` (ver `DevSecurityConfig`) libera o Swagger UI sem login, desliga o CSRF e habilita
**HTTP Basic** com um usuário em memória (`DEV_USER`/`DEV_PASSWORD` no `.env`, padrão `dev`/`dev`):

```sh
curl -u dev:dev "http://localhost:8080/api/v1/categoria?sortInfo=nome&decrescente=false"

curl -u dev:dev -X POST http://localhost:8080/api/v1/categoria \
     -H "Content-Type: application/json" \
     -d '{"nome":"Saude","cor":"#FF0000","fatorOrdem":1}'
```

No Swagger UI, o botão **Authorize** oferece os três esquemas (`bearerAuth`, `basicAuth`, `oauth2`) e
passa a mandar o header em todas as chamadas.

> O perfil `dev` não altera nada no perfil padrão: lá o Basic não existe, o Swagger continua exigindo
> login e o navegador segue sendo redirecionado ao Google.

### Obtendo um `id_token` no Postman

Em *Authorization → OAuth 2.0*, use `Authorization Code` com:

- Auth URL: `https://accounts.google.com/o/oauth2/v2/auth`
- Access Token URL: `https://oauth2.googleapis.com/token`
- Callback URL: `https://oauth.pstmn.io/v1/callback` (precisa estar registrada no Google Console)
- Scope: `openid profile email`

Na resposta, use o **`id_token`** (não o `access_token`, que é opaco e não é um JWT).

## Contribuição

Se desejar contribuir, siga os passos:

1. Faça um fork do repositório
2. Crie uma branch (`feature/nova-funcionalidade`)
3. Envie um pull request

---

Projeto em desenvolvimento 🚀

