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
    "id": "9b1f0c2e-5a7d-4f31-8c60-2a1e9d4b7f03",
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

## Banco e migrações

O schema é **versionado com Flyway** e o Hibernate roda em `validate`. Ele não cria nem altera nada:
só confere no boot se o mapeamento bate com o banco, e **falha alto** se divergir — em vez de
descobrir a divergência no primeiro INSERT em produção.

```
src/main/resources/db/migration/
  V1__schema_inicial.sql
```

Foi uma troca deliberada de `ddl-auto: create-drop`, que apagava tudo a cada boot. Ela cabia
enquanto nada precisava sobreviver; não cabe mais.

Migrar depois custaria um `baseline`; migrar agora custa uma migração, porque ainda não existe base
de produção para preservar.

### Configuração

Tudo por variável de ambiente, com default de desenvolvimento (ver `.env.example`):

| Variável | Default | Observação |
|---|---|---|
| `DB_URL` | Postgres local | em provedor gerenciado, use o endpoint do pooler |
| `DB_POOL_SIZE` | `10` | free tier do Supabase: use `5` |
| `JPA_DDL_AUTO` | `validate` | não troque para `update` com dado real |
| `JPA_SHOW_SQL` | `false` | ligue só na máquina de desenvolvimento |
| `FLYWAY_ENABLED` | `true` | desligado apenas nos testes |

Qualquer Postgres serve — Supabase, Neon, Railway. **Se for Supabase**: use o endpoint do pooler
(Supavisor, porta 6543), porque a conexão direta é IPv6 em projetos novos e vários hosts não têm
saída IPv6; e acrescente `?prepareThreshold=0` na URL, senão o pooler em transaction mode quebra o
prepared statement do driver JDBC.

### Testes

Os testes rodam em H2 com `create-drop` e **Flyway desligado** — as migrações são SQL de Postgres.
Quem garante que a migração continua batendo com as entidades é o `validate` do perfil real: se o
mapeamento e o SQL divergirem, a aplicação não sobe.

## Agendamento (Quartz)

O job store é **JDBC**, não memória. As tabelas do Quartz são criadas pela migração
`V2__quartz.sql` — o script oficial do Quartz 2.3.2 para Postgres, versionado aqui porque schema tem
um dono só, o Flyway (`spring.quartz.jdbc.initialize-schema: never`).

Isso corrige uma falha que era invisível: com `RAMJobStore`, **todo restart apagava os triggers**.
Os lembretes continuavam no banco e nunca mais disparavam, sem erro nenhum no log, porque nada
reagendava no boot. Enquanto o banco também era `create-drop` a inconsistência não aparecia — os
lembretes sumiam junto. Persistir o banco é o que tornava esse bug visível.

A chave do job é o `uuid` do lembrete, não o id sequencial: agora que ela é gravada no banco, a
estabilidade dessa chave passou a importar.

### Disparo vencido (misfire)

Com job store persistente surge um caso que antes não existia: o que fazer com o que venceu enquanto
a aplicação esteve fora. O Quartz chama isso de *misfire* e, por padrão, dispara tudo de uma vez no
boot — uma enxurrada de notificações vencidas.

O que **não** dá para fazer é mandar o Quartz descartar o misfire. Os triggers daqui são de disparo
único e quem reagenda é o próprio `LembreteExecutorJob`: trigger descartado significa lembrete órfão
para sempre. Então o disparo acontece e o filtro fica no job — ele roda, **não notifica**, e
reagenda para a próxima ocorrência legítima.

A fronteira é `TOLERANCIA_ATRASO`, hoje 5 minutos: um reinício rápido ainda notifica, uma janela de
indisponibilidade não. Disparo vencido também **não consome cota** — o lembrete não foi entregue,
então não faz sentido cobrar dele.

### Uma instância

`isClustered` está `false` porque hoje roda uma instância só. Com duas instâncias e `false`, **as
duas disparariam o mesmo lembrete** — é o modo cluster que garante que só uma pegue cada trigger.
A troca é a variável `QUARTZ_CLUSTERED`; o `instanceId: AUTO` já está no lugar.

## Identidade do lembrete

O lembrete tem **duas chaves, com papéis separados**:

| Chave | Tipo | Papel |
|---|---|---|
| `id` | `BIGINT` sequencial | chave da tabela. Nunca sai da camada de persistência |
| `uuid` | `UUID` | identidade pública. É por ela que a aplicação procura o lembrete |

A tabela mantém o id sequencial porque ele é a chave certa para o banco: preserva localidade no
índice B-tree e deixa as FKs em 8 bytes, coisas que um UUID aleatório como chave primária custa.
O que muda é que **nada na aplicação consulta por ele** — todo caminho de busca entra por `uuid`.

```
PUT    /api/v1/lembrete/{uuid}
PATCH  /api/v1/lembrete/{uuid}
DELETE /api/v1/lembrete/{uuid}

DELETE /api/v1/lembrete/9b1f0c2e-…-2a1e9d4b7f03   ->  404 se não existir
DELETE /api/v1/lembrete/1                         ->  400, não é UUID
```

Na saída da API só aparece `uuid`; o id sequencial não é exposto, justamente para nada externo
passar a depender dele.

Três consequências que motivaram a mudança:

- **A identidade pública nasce no domínio**, no construtor de `Lembrete`, não no `INSERT`. O
  lembrete já sabe quem é antes de existir no banco.
- **A chave do job no Quartz é o `uuid`.** Não há tabela de-para, e a chave não muda se a base for
  recarregada — o que passa a importar quando o job store deixar de ser memória.
- **Nada externo depende de um número de sequência**, que vaza volume de dados e amarra a API à
  estratégia de persistência.

No repositório, só `findByUuid` é usado; os métodos herdados que recebem `Long` (`findById`,
`deleteById`) ficam restritos à camada de persistência. Categoria continua só com id sequencial: é
dado de apoio, não trafega como identidade pública. O `CRUDUseCase` foi parametrizado pelo tipo do
identificador (`CRUDUseCase<I, O, ID>`) para os dois conviverem.

## Recorrência por dias da semana

Não existe um campo "dias úteis": é a lista dos cinco dias em `recorrencia.diasDaSemana`. Os valores
aceitos são `SEGUNDA`, `TERCA`, `QUARTA`, `QUINTA`, `SEXTA`, `SABADO` e `DOMINGO`.

```json
{
    "titulo": "Daily",
    "categoriaId": 1,
    "recorrencia": { "diasDaSemana": ["SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA"] },
    "notificacao": { "metodo": ["e-mail"], "horario": "09:00" }
}
```

Isso vira a expressão `0 0 9 ? * MON,TUE,WED,THU,FRI`. Recorrência de calendário **exige**
`notificacao.horario` — sem ele a requisição volta 422.

## Feriados e dias úteis

`MON,TUE,WED,THU,FRI` sabe o que é dia da semana, não o que é feriado. Para isso existe
`recorrencia.politicaDiaUtil`:

| Valor | O que faz | Quando usar |
|---|---|---|
| `IGNORAR` (padrão) | o calendário não interfere | tomar remédio — feriado não suspende |
| `PULAR` | a ocorrência em dia não útil não acontece | daily de equipe |
| `ADIAR` | anda para o próximo dia útil, mesmo horário | tarefa que pode esperar |
| `ANTECIPAR` | volta para o dia útil anterior | boleto, que não pode atrasar |

```json
{
    "recorrencia": {
        "diasDaSemana": ["SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA"],
        "politicaDiaUtil": "PULAR"
    },
    "notificacao": { "metodo": ["e-mail"], "horario": "09:00" }
}
```

**Uma regra, dois leitores.** A política vale igual na projeção (`proximasExecucoes`) e no disparo
real. É por isso que um lembrete com política não usa mais o cron permanente do Quartz: aquele
trigger só entende dia da semana e dispararia no feriado, fazendo a prévia mentir. Com política, o
lembrete cai no caminho de disparo único mais reagendamento — o mesmo que o intervalo já usava.

**Origem dos feriados.** `FeriadoNacionalBrasilAdapter` **calcula** o calendário nacional: datas
fixas mais as móveis derivadas da Páscoa (Carnaval, Sexta-feira Santa, Corpus Christi). Escolher uma
fonte calculável em vez de uma API elimina de uma vez rede no caminho quente, indisponibilidade de
terceiro, cache negativo e dado velho — o cache vira memoização por ano, com teto de anos guardados.
Feriado estadual e municipal não são calculáveis: aí sim é preciso uma fonte externa, e o ponto de
troca é implementar `FeriadoPort`, sem tocar no domínio.

**Limites de entrada.** A política é recusada com 422 em dois casos:

- **intervalo abaixo de um dia** — 1 em 1 minuto são 1440 ocorrências por dia, e um feriado emendado
  em fim de semana produz mais de 4 mil ocorrências consecutivas a descartar, por projeção. Como o
  intervalo é entrada do usuário, aceitar seria deixá-lo escolher quanto a aplicação gasta.
- **junto de `datasEspecificadas`** — ali o usuário apontou o dia a dedo, e o calendário não
  sobrescreve escolha explícita.

## Fuso horário

Toda data trafega e é persistida como hora de parede, sem offset (`LocalDateTime`, serializado como
`2026-08-27T09:00:00`). O back **não converte nada**: ele grava o que o front mandou.

O detalhe que importa no deploy é o outro lado dessa moeda: na hora de agendar, o Quartz interpreta
essa hora de parede **no fuso do servidor** (`ZoneId.systemDefault()`), e o filtro de datas futuras
compara com o `LocalDateTime.now()` do servidor. Enquanto servidor e usuário estiverem no mesmo fuso
— desenvolvimento local — nada aparece. Num host em UTC, com usuário em São Paulo:

| O que o front manda | O que o servidor em UTC faz |
|---|---|
| `"horario": "09:00"` | dispara às 09:00 UTC, ou seja **06:00** para o usuário |
| `"dataInicio"` daqui a 1h | vê como **vencida** (o `now()` dele está 3h à frente) e descarta |

Ou seja, o back é neutro no transporte, mas não no agendamento. Rodar o servidor no mesmo fuso dos
usuários (`TZ=America/Sao_Paulo`, ou `-Duser.timezone`) resolve enquanto houver um fuso só; um app
com usuários em fusos diferentes precisa trafegar offset (`OffsetDateTime`) e guardar o fuso
desejado do lembrete. `FusoHorarioTest` fixa esse comportamento para que a mudança seja uma decisão
explícita.

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

