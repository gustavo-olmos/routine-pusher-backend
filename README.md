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

#### Resposta

Além do estado salvo, a resposta traz `proximasExecucoes`: uma prévia das próximas 5 execuções
derivada da recorrência. Não é estado persistido — é recalculada a cada leitura, respeita a validade
(`dataFim`) e a cota restante (`quantidade`), e por isso encurta ou some conforme o lembrete avança.

```json
{
    "id": 1,
    "titulo": "Pomodoro",
    "descricao": "de 25 em 25 minutos",
    "status": "PENDENTE",
    "categoria": { "id": 1, "nome": "Foco", "cor": "#00897B", "fatorOrdem": 1 },
    "recorrencia": { "quantidade": null, "intervaloMinutos": 25 },
    "notificacao": {
        "id": 1,
        "metodo": ["pop-up"],
        "proximaExecucao": "2026-08-27T20:38:26",
        "dataInicio": "2026-08-27T20:13:26"
    },
    "proximasExecucoes": [
        "2026-08-27T20:38:26",
        "2026-08-27T21:03:26",
        "2026-08-27T21:28:26",
        "2026-08-27T21:53:26",
        "2026-08-27T22:18:26"
    ]
}
```

`proximaExecucao` continua sendo o disparo realmente agendado no Quartz; `proximasExecucoes[0]`
coincide com ele. O restante da lista é projeção: o agendamento segue acontecendo um disparo de cada
vez, e concluir ou alterar o lembrete refaz a série.

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

O perfil `dev` (ver `DevSecurityConfig`) coloca **a aplicação inteira em whitelist**: nenhuma rota
exige autenticação e o CSRF fica desligado, para exercitar qualquer endpoint sem fricção.

```sh
curl "http://localhost:8080/api/v1/categoria?sortInfo=nome&decrescente=false"

curl -X POST http://localhost:8080/api/v1/categoria \
     -H "Content-Type: application/json" \
     -d '{"nome":"Saude","cor":"#FF0000","fatorOrdem":1}'
```

> É deliberadamente permissivo e serve só para teste local — não suba com `dev` em lugar nenhum
> exposto. O perfil padrão não muda: lá o Swagger continua exigindo login e o navegador segue sendo
> redirecionado ao Google.

No Swagger UI, o botão **Authorize** oferece `bearerAuth` e `oauth2`, e passa a mandar o header em
todas as chamadas — necessário no perfil padrão, dispensável no `dev`.

### Testando rápido depois de subir

O schema é `create-drop` e os jobs do Quartz vivem em memória: **nada sobrevive ao restart**, por
escolha. Por isso o teste manual parte sempre de um banco vazio.

Para não perder tempo com isso, `scripts/routine-pusher.postman_collection.json` é uma coleção
Postman pronta: 22 requisições em 4 pastas, encadeadas para rodar de ponta a ponta no **Collection
Runner**. A pasta 1 cria a categoria e guarda o id numa variável de coleção; as pastas seguintes
dependem dele.

1. Postman > **Import** > arraste o arquivo (ou *Raw text*, colando o conteúdo)
2. Suba a aplicação no perfil `dev`, que não exige autenticação
3. Rode a coleção, **desmarcando a pasta 4 (SSE)** — ela mantém a conexão aberta e travaria a execução

Cada requisição traz assertivas (`pm.test`), então o Runner dá pass/fail em vez de só devolver
resposta. A pasta 3 cobre os erros esperados (400, 404, 405, 409, 422), útil para conferir o
`GlobalExceptionHandler` sem forçar nada na mão. `cor` e `fatorOrdem` são sorteados a cada execução,
então dá para rodar várias vezes seguidas sem reiniciar a aplicação.

Alternativa sem arquivo nenhum: **Import > Link > `http://localhost:8080/v3/api-docs`** gera a
coleção a partir do OpenAPI — mas sem payloads de exemplo, sem encadeamento e sem assertivas.

Para ver os disparos chegando, deixe o SSE aberto em outro terminal:

```sh
curl -N http://localhost:8080/api/v1/notificar/sse
```

Duas regras que economizam tentativa e erro ao montar um lembrete novo:

- recorrência por **intervalo** exige `notificacao.dataInicio` (é a base do cálculo)
- recorrência por **cron** (`diasDaSemana`, `diasFixosNoMes`) exige `notificacao.horario`

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

