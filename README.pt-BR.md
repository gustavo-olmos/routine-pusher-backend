# Routine Pusher — API

API de lembretes com **agendamento real**: o lembrete não é uma linha numa tabela esperando alguém
consultar, é um *trigger* persistido no Quartz que dispara na hora marcada e sobrevive a restart.
Em cima disso, duas coisas que o projeto existe para demonstrar: **criação de lembrete a partir de
uma frase em português**, com o contrato que o modelo preenche derivado do próprio DTO, e **sessão
anônima**, para que um visitante use o demo inteiro sem cadastrar nada.

No ar em **`https://api.rotafin.com.br`** — raiz e `/actuator/health` respondem sem autenticação.

Projeto de estudo de arquitetura: cada decisão não óbvia está comentada no código, no ponto onde
ela foi tomada, com o problema que a motivou.

---

## Stack

| Peça | Papel |
|---|---|
| **Java 17 + Spring Boot 3.3** | base da aplicação |
| **PostgreSQL + Flyway** | schema versionado; Hibernate roda em `validate`, nunca altera nada |
| **Quartz (JobStore JDBC)** | agendamento que vive no banco e sobrevive a restart |
| **Spring Security + OAuth2** | sessão Google e Bearer (`id_token`), ambos **opcionais** |
| **WebFlux (SSE)** | canal de notificação do navegador, escopado por sessão |
| **Gemini / Anthropic** | extração estruturada da frase; o provedor é variável de ambiente |
| **MapStruct + Lombok** | mapeamento entidade ↔ domínio ↔ DTO |
| **springdoc-openapi** | Swagger UI |
| **Docker** | imagem multi-stage, usada no deploy |

Não há fila de mensageria. O disparo acontece dentro do processo, no job do Quartz, e chega ao
navegador por SSE.

---

## Como rodar local

### 1. Postgres

Qualquer Postgres serve. Com Docker (nesta máquina ele vive dentro da WSL):

```sh
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=routine-pusher postgres:16
```

Não é preciso criar tabela nenhuma: o Flyway aplica as 6 migrações no primeiro boot, incluindo as
tabelas do Quartz.

### 2. Variáveis

```sh
cp .env.example .env      # o .env é ignorado pelo git
```

A única sem default é `DB_PASSWORD`. Tudo o mais roda com o padrão de desenvolvimento — inclusive
sem chave de IA e sem credencial do Google (veja *Configuração*).

### 3. Subir

```sh
./mvnw spring-boot:run
```

Swagger em `http://localhost:8080/swagger-ui.html`, saúde em `/actuator/health`.

> **Perfil `dev`** (`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`) põe a aplicação inteira
> em `permitAll` e desliga CSRF, para Postman e curl não esbarrarem em autenticação. É só para
> teste local — **nenhuma verificação de segurança feita contra o perfil `dev` vale**, porque lá
> tudo responde 200.

Há uma coleção do Postman pronta em [`scripts/`](scripts/routine-pusher.postman_collection.json).

---

## Arquitetura

Três camadas, com a dependência apontando sempre para dentro. A regra não é só convenção: está
fixada em [`ArchitecturalTest`](src/test/java/com/routine/pusher/infrastructure/arch/ArchitecturalTest.java),
e quebrá-la reprova a build.

```
core/            domínio puro — regra de recorrência, strategies, enums, portas
  domain/        lembrete, notificacao, recorrencia, categoria, sessao, feriado
  trigger/       quando a série termina: validade, quantidade, datas, ilimitado
application/     orquestração — services, use cases, controllers, jobs do Quartz
infrastructure/  o mundo externo — segurança, CORS, Quartz, filtros, handler de erro
```

Dois padrões carregam o domínio:

- **Strategy de notificação** (`CronStrategy`, `IntervaloStrategy`, `DatasEspecificadasStrategy`)
  responde *quando dispara*. Validade (`dataFim`) e cota (`quantidade`) **não** moram nelas: valem
  para as três, então ficam em `Notificacao` — espalhá-las seria repetir a mesma regra três vezes.
- **Strategy de trigger** responde *quando a série acaba*: validade, quantidade, datas específicas
  ou ilimitado.

E dois caminhos com contratos deliberadamente diferentes: o de **agendamento** (um disparo por vez,
pode mutar estado) e o de **leitura** (`proximasExecucoes`, puro, N ocorrências). Unificá-los
transformaria um GET em escrita.

---

## API

Tudo sob `/api/v1`. Nenhuma rota de API exige login: a identidade do visitante é a sessão anônima
do cookie, e o escopo é aplicado em toda consulta.

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/lembrete` | cria o lembrete e agenda o primeiro disparo — `201` + `Location` |
| `GET` | `/lembrete?sortInfo=<campo>&decrescente=<bool>` | lista os da sessão (**os dois parâmetros são obrigatórios**) |
| `GET` | `/lembrete/{uuid}` | busca um lembrete — o destino do `Location` da criação |
| `PUT` | `/lembrete/{uuid}` | substitui o lembrete, **refazendo o agendamento** |
| `PATCH` | `/lembrete/{uuid}/detalhes` | edita título, descrição e categoria **preservando o agendamento** |
| `PATCH` | `/lembrete/{uuid}` | conclui |
| `DELETE` | `/lembrete/{uuid}` | exclui o lembrete e cancela o trigger — `204` |
| `POST` | `/categoria` | cria categoria na lista da sessão |
| `GET` | `/categoria?sortInfo=<campo>&decrescente=<bool>` | lista |
| `GET` · `PUT` · `DELETE` | `/categoria/{id}` | busca, atualiza, exclui |
| `POST` | `/chat/interpretacao` | **transforma uma frase em rascunho de lembrete, sem salvar** |
| `POST` | `/chat/lembrete` | *obsoleto:* interpreta e salva numa chamada só, sem revisão |
| `GET` | `/sessao` | consulta a sessão atual (cria uma se ainda não houver) |
| `DELETE` | `/sessao` | o "esquecer-me": apaga sessão, lembretes e agendamentos; expira o cookie |
| `GET` | `/notificar/sse` | stream SSE dos disparos da sessão |

Fora do `/api/v1`: `GET /` (cartão de visita), `/actuator/health`, `/swagger-ui.html`.

O id de outro visitante responde **404**, não 403 — inclusive para categoria, cujo id é sequencial
e adivinhável.

### Criar um lembrete

```http
POST /api/v1/lembrete
```

```json
{
  "titulo": "Beber água",
  "descricao": "Garrafa de 500ml",
  "categoriaId": 1,
  "recorrencia": { "intervaloHoras": 2 },
  "notificacao": {
    "metodo": ["pop-up"],
    "dataInicio": "2026-11-23T08:00:00",
    "dataFim": "2026-11-23T18:00:00"
  }
}
```

Três formas de recorrência, mutuamente exclusivas:

- **Intervalo** — `intervaloDias` / `intervaloHoras` / `intervaloMinutos`. O total precisa somar ao
  menos **5 minutos**. `dataInicio` é a **base da contagem**, não o primeiro disparo: o primeiro
  disparo é `dataInicio + intervalo`.
- **Calendário** — `diasDaSemana`, `diasFixosNoMes` ou `posicaoDaSemanaNoMes`, com
  `notificacao.horario` (`HH:mm`). Vira expressão cron internamente.
- **Datas a dedo** — `notificacao.datasEspecificadas`, sem recorrência.

Limitadores opcionais: `recorrencia.quantidade` (total de disparos), `notificacao.dataFim`
(validade) e `recorrencia.politicaDiaUtil` (`PULAR`, `ADIAR`, `ANTECIPAR` quando a ocorrência cai
em fim de semana ou feriado nacional).

Omitir recorrência **e** datas específicas é recusado com `422` explicativo: um lembrete que não
sabe quando disparar não é um lembrete.

### Resposta

`201 Created`, com `Location` apontando para `/api/v1/lembrete/{uuid}` e o lembrete completo no corpo.

```json
{
  "uuid": "9b1f0c2e-5a7d-4f31-8c60-2a1e9d4b7f03",
  "titulo": "Beber água",
  "descricao": "Garrafa de 500ml",
  "status": "PENDENTE",
  "categoria": { "id": 1, "nome": "Importante", "cor": "#E53935", "fatorOrdem": 1 },
  "recorrencia": { "quantidade": null, "intervaloHoras": 2 },
  "notificacao": {
    "id": 1,
    "metodo": ["pop-up"],
    "proximaExecucao": "2026-11-23T10:00:00",
    "dataInicio": "2026-11-23T08:00:00",
    "dataFim": "2026-11-23T18:00:00"
  },
  "proximasExecucoes": [
    "2026-11-23T10:00:00",
    "2026-11-23T12:00:00",
    "2026-11-23T14:00:00",
    "2026-11-23T16:00:00",
    "2026-11-23T18:00:00"
  ]
}
```

O `uuid` é a identidade pública — o id sequencial da tabela nunca sai da persistência.

`proximasExecucoes` é **prévia calculada, não estado persistido**: recalculada a cada leitura,
respeita validade e cota restante, e por isso encurta ou some conforme o lembrete avança.
`proximaExecucao` é o disparo realmente agendado no Quartz, e coincide com `proximasExecucoes[0]`;
o resto é projeção — o agendamento acontece um disparo de cada vez.

### Erros

Resposta uniforme, de um `@RestControllerAdvice` que estende `ResponseEntityExceptionHandler` (um
catch-all de `Exception` transformava erro de cliente em 500):

```json
{
  "timestamp": "2026-11-23T09:14:02",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Campos inválidos",
  "caminho": "/api/v1/lembrete",
  "camposInvalidos": { "titulo": "O título do lembrete é obrigatório" }
}
```

`camposInvalidos` só aparece em falha de validação. `422` para regra de negócio (agendamento
impossível, excluir categoria em uso), `429` para limite de sessão estourado, `404` para o que não
é seu.

---

## Sessão anônima

Ninguém cria conta para testar um site que veio de um post. A identidade nasce no primeiro request
a `/api/**`, viaja no cookie **`RP_SESSAO`** (`HttpOnly`, `SameSite=Lax`, `Secure` quando a conexão
é HTTPS) e tudo que o visitante cria pertence a ela.

| | |
|---|---|
| Janela de inatividade | **48 h** — qualquer chamada renova |
| Validade do cookie | 7 dias (maior de propósito: quem manda é o servidor) |
| Renovação de `ultimo_acesso` | no máximo 1× por hora, para não virar um UPDATE por clique |
| Faxina | job do Quartz de hora em hora: apaga sessões expiradas, seus lembretes e agendamentos |
| Teto de lembretes | **10** por sessão |
| Teto de chamadas de IA | **10** por sessão (consumidas por UPDATE atômico → `429`) |

Toda sessão nova nasce com duas categorias — **Importante** e **Urgente** — criadas em tempo de
execução, não por migração. São linhas comuns da lista: podem ser renomeadas, recoloridas e
apagadas.

**O `SameSite=Lax` tem uma consequência de infraestrutura:** front e API precisam morar no mesmo
domínio registrável (`site.com` e `api.site.com`). Em domínios diferentes o cookie não viaja em
XHR, e a falha é silenciosa — criar devolve 200, listar devolve `[]`, porque cada requisição chega
como um visitante novo. Em desenvolvimento, `localhost:4200` e `localhost:8080` são o mesmo site.

É também por isso que a proteção de CSRF está desligada em `/api/**`: site de terceiro não consegue
anexar um cookie `Lax` num POST cross-site, e os GETs são somente leitura. O token de CSRF só
atrapalharia Postman e Swagger.

---

## Chat de IA

O modelo nunca salva nada. Ele rascunha, o usuário revisa, e uma criação comum confirma:
**interpretar → revisar → criar**.

**1. Interpretar.**

```http
POST /api/v1/chat/interpretacao
{ "frase": "me lembra de beber água a cada 2 horas até as 18h", "agora": "2026-11-23T08:00:00" }
```

```json
{
  "lembrete": { "titulo": "Beber água", "categoriaId": 1, "recorrencia": { … }, "notificacao": { … } },
  "proximasExecucoes": ["2026-11-23T08:00:00", "2026-11-23T10:00:00", "…"]
}
```

`lembrete` é exatamente o corpo que o `POST /api/v1/lembrete` aceita. `proximasExecucoes` sai do
mesmo domínio da criação, calculado sobre um lembrete que nunca foi salvo. `agora` é o relógio de
quem pede, para "amanhã às 9h" resolver no fuso do usuário e não no do servidor.

O rascunho passa por todas as regras da criação antes de aparecer: Bean Validation, intervalo
mínimo, política de dia útil e "isto vai disparar algum dia". Criação e simulação usam o mesmo
`montarNovo` do `LembreteService`, então um rascunho aprovado aqui só falha na confirmação se o
mundo mudar no meio: a sessão encher, ou a categoria ser excluída.

As travas vão da mais barata para a mais cara: o teto de lembretes da sessão (um `COUNT`) é
conferido **antes** de gastar a cota de IA, porque rascunho que não pode ser salvo é chamada de
modelo jogada fora. `429` para qualquer dos dois limites, `422` quando o modelo monta algo inválido
ou que nunca dispararia.

**2. Revisar.** O cliente mostra o rascunho e a prévia das execuções. O usuário pode ajustar.

**3. Criar.** `POST /api/v1/lembrete` com o objeto `lembrete`. Confirmar não consome cota de IA. Não
há edição pelo chat nem conversa guardada: o chat é descartável, e edição é pelo `PUT`.

O `POST /api/v1/chat/lembrete`, que interpretava e salvava numa chamada só, continua no ar para o
front publicado e marcado como obsoleto no Swagger. Sai quando o cliente migrar.

O que o projeto quer mostrar aqui:

- **O JSON Schema é derivado de `LembreteInputDTO` em tempo de execução** (victools), com as
  descrições vindo das `@JsonPropertyDescription` do próprio DTO. Contrato escrito à mão apodrece
  em silêncio quando o domínio muda; derivado, não tem como divergir.
- **As anotações de Bean Validation viram `required` no schema.** A mesma `@NotNull` que recusa o
  pedido de um humano passa a obrigar o modelo.
- **O que o modelo devolve é entrada externa como qualquer outra:** passa pelas mesmas validações
  do POST manual.
- **Rota e contrato neutros.** `ContratoLembreteIA` concentra schema, prompt e contexto; os
  adapters (`GeminiChatClient`, `AnthropicChatClient`) só falam o protocolo do provedor. Trocar de
  provedor é a variável `IA_PROVEDOR`, não um deploy — e a URL é `/chat`, não o nome de ninguém.

Sem chave configurada o endpoint responde indisponível e **o resto da aplicação sobe normalmente**.

---

## Banco e migrações

O schema é versionado com **Flyway**; o Hibernate roda em `validate` e **falha alto** no boot se o
mapeamento divergir do banco, em vez de descobrir a divergência no primeiro INSERT.

```
src/main/resources/db/migration/
  V1__schema_inicial.sql          lembrete, notificacao, recorrencia, categoria
  V2__quartz.sql                  tabelas do Quartz (script oficial 2.3.2)
  V3__sessao_anonima.sql          a identidade do visitante
  V4__cota_de_ia_por_sessao.sql   contador de chamadas na própria sessão
  V5__categorias_do_demo.sql      lista global de categorias (superada pela V6)
  V6__categoria_por_sessao.sql    categoria passa a pertencer à sessão
```

Duas tabelas têm chave dupla (`lembrete`, `sessao_anonima`): `id BIGINT` sequencial como chave
interna — localidade no índice B-tree e FKs de 8 bytes — e `uuid` como chave de negócio, que é a
que a API expõe e a que identifica o job no Quartz.

A **V6 não destrói dado**: cada categoria global em uso vira cópia da sessão que a usa, os
lembretes são repontados, e só então as globais saem. Foi testada contra uma cópia de banco real.

---

## Configuração

Tudo por variável de ambiente, com default de desenvolvimento. Referência completa e comentada em
[`.env.example`](.env.example).

| Variável | Default | Observação |
|---|---|---|
| `DB_URL` | Postgres local | em provedor gerenciado, use o endpoint do *pooler* |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / — | a senha é a única variável sem default |
| `DB_POOL_SIZE` | `10` | free tier do Supabase: `5` |
| `JPA_DDL_AUTO` | `validate` | não troque para `update` com dado real |
| `JPA_SHOW_SQL` | `false` | ligue só na máquina de desenvolvimento |
| `FLYWAY_ENABLED` | `true` | desligado apenas nos testes |
| `QUARTZ_THREADS` | `5` | |
| `QUARTZ_CLUSTERED` | `false` | ligue **junto** com a segunda instância, nunca antes nem depois |
| `IA_PROVEDOR` | `gemini` | `gemini` ou `anthropic`; só o escolhido vira bean |
| `GEMINI_API_KEY` / `GEMINI_MODEL` | — / `gemini-3.5-flash-lite` | chave gratuita no AI Studio, sem cartão |
| `ANTHROPIC_API_KEY` / `ANTHROPIC_MODEL` | — / `claude-haiku-4-5` | exige crédito de API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | origens exatas, separadas por vírgula; sem curinga |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | — | **opcionais** |
| `PORT` | `8080` | injetada pela plataforma |

**O login do Google é opcional de verdade:** sem as duas variáveis a aplicação sobe e a API do demo
funciona inteira — só não há sessão autenticada, e o Swagger fica em 401.

Isso já foi falso e derrubou o primeiro deploy. A registração vivia em
`spring.security.oauth2.client.registration.google` com default vazio, na crença de que valor vazio
desligava o login: o Spring Boot valida as registrações **declaradas** e recusa `client-id` vazio,
então a chave presente no YAML bastava para matar o boot. Hoje ela mora em `app.google.*` e só
nasce com credencial (`GoogleOAuthConfig`). Fixado em `SegurancaSemGoogleTest`.

---

## Testes

```sh
./mvnw test
```

180 testes em H2 (`create-drop`, Flyway desligado — as migrações são SQL de Postgres), cobrindo o
motor de recorrência, dias úteis e feriados, fuso, escopo de sessão, dialeto dos dois provedores de
IA, as rotas públicas e as regras de arquitetura.

> **Suíte verde não valida o `application.yml` de produção.** O
> `src/test/resources/application.yml` **sombreia** o principal: dá para quebrar o YAML real com a
> suíte inteira passando. O que pega isso é rodar a imagem:
>
> ```sh
> docker run --rm --memory=512m -p 8080:8080 --env-file .env.production routine-pusher:latest
> ```

---

## Deploy

A aplicação é um **processo de vida longa com agendador embutido**. Isso, e não preferência, é o
que elimina algumas opções: precisa de um processo acordado entre requisições, de HTTPS com domínio
estável e de um Postgres. Plano gratuito que hiberna por inatividade não serve — agendador dormindo
não lembra. Função serverless também não.

Hoje roda em **AWS Lightsail Containers** (`sa-east-1`, plano `nano`, 512 MB) com banco no
**Supabase**. O passo a passo, as permissões de IAM, as armadilhas do pooler e as medições que
justificaram o plano estão em **[`deploy/lightsail/README.md`](deploy/lightsail/README.md)**.

O `Dockerfile` na raiz é multi-stage e não tem nada de específico de plataforma — trocar de
provedor não vira reescrita. O que já vem resolvido nele e no `application.yml`:

- **`PORT`** injetada pela plataforma (`server.port: ${PORT:8080}`).
- **`/actuator/health` sem autenticação.** Health check que exige credencial faz a plataforma
  concluir que a instância está doente e reiniciar o container em laço.
- **Desligamento gracioso** (`server.shutdown: graceful` + `wait-for-jobs-to-complete-on-shutdown`).
  Um SIGTERM no meio de um disparo deixaria o trigger travado em `ACQUIRED` até o `misfireThreshold`.
- **`forward-headers-strategy: framework`.** Atrás do proxy, sem ler `X-Forwarded-*` o Spring monta
  o redirect do OAuth em `http://` e o cookie sai sem `Secure`.
- **`TZ=America/Sao_Paulo`.** O agendamento usa hora sem fuso, então o relógio do servidor precisa
  ser o do público; container roda em UTC por padrão, o que faria todo lembrete das 9h disparar às
  6h. É mitigação, não correção — ver abaixo.
- **`-XX:TieredStopAtLevel=1`.** Derrubou o boot de 158s para 103s no `nano` (0,25 vCPU). O gargalo
  ali é partida, não vazão; se um dia o tráfego inverter isso, é a primeira flag a sair.

---

## Limitações conhecidas

São escopo lido como escopo, não lacunas esquecidas:

- **Notificação só chega com a aba aberta.** O canal é SSE. Web Push exigiria service worker e
  identidade persistente — recusados de propósito num demo anônimo.
- **Lembrete com hora, não com fuso.** O domínio trabalha com `LocalDateTime`; o fuso é fixado no
  container. A correção real é cada lembrete guardar o fuso de quem o criou.
- **A cota de IA é burlável limpando o cookie.** No free tier do Gemini o teto diário do provedor
  limita o prejuízo a "o chat para no dia"; com provedor pago, é preciso um limite por IP antes.
- **Retenção de 48 h.** Lembrete marcado para daqui a duas semanas não sobrevive se o visitante não
  voltar em dois dias. Retenção de verdade pede conta, não sessão anônima.
- **Nada impede apagar todas as categorias**, e sem categoria não se cria lembrete (`categoriaId` é
  obrigatório). O tratamento está no front.
- **Uma instância só.** Com duas e `QUARTZ_CLUSTERED=false`, as duas disparam o mesmo lembrete.
- **Sem CI.** O deploy é manual, a partir da máquina de desenvolvimento.
