# Routine Pusher — API

*[Versão em português](README.pt-BR.md)*

A reminder API where the reminder is **actually scheduled**: not a row waiting for someone to poll
it, but a Quartz trigger persisted in the database that fires at the appointed time and survives a
restart. On top of that, the thing this project exists to demonstrate — **turning a sentence in
plain Portuguese into a validated domain object**, with the contract the model fills in derived
from the DTO itself.

Live at **`https://api.rotafin.com.br`** — the root and `/actuator/health` answer without
authentication.

Java 17 · Spring Boot 3.3 · PostgreSQL + Flyway · Quartz (JDBC job store) · WebFlux (SSE) ·
Gemini / Anthropic · MapStruct · Docker on AWS Lightsail

---

## What this project demonstrates

### The AI contract is derived from the DTO, not written by hand

The usual first attempt is to ask a model for free text and then parse it. This does the opposite:
the model fills in **a contract the domain already defines**.

The JSON Schema handed to the provider is generated at runtime from `LembreteInputDTO` (victools +
Jackson module), and the field descriptions come from the `@JsonPropertyDescription` annotations on
that same record. Swagger documentation and model instructions are the same text, written once.

A schema maintained by hand rots silently the moment the domain changes — a new required field, a
renamed property, a narrowed enum — and nothing fails until a model quietly omits it in production.
A derived schema cannot drift, because there is no second copy to drift from.

Two dialect details that only show up against real providers, both handled in
[`ContratoLembreteIA`](src/main/java/com/routine/pusher/application/external/client/ContratoLembreteIA.java):
schemas are fully inlined (Gemini's subset is OpenAPI and does not resolve `$ref`/`$defs`, which
victools emits for `LocalDateTime`), and date fields are declared as plain strings, because
generators emit `pattern` for `java.time` and Anthropic's structured output rejects `pattern`.

### Human input and model output go through the same validation

Structured output guarantees the **shape** of the JSON. Required fields, sizes and value ranges are
*our* contract, and a contract is checked at the door — whichever side the data came from.

So the model's answer is run through the same Bean Validation constraints the controller applies to
a hand-written `POST`
([`LembreteChatService.validar`](src/main/java/com/routine/pusher/application/service/LembreteChatService.java)).
The same annotations also feed the schema: `JakartaValidationModule` turns every `@NotNull` into a
`required` entry, so the rule that rejects a human's request is the rule that tells the model what
it must fill in. One declaration, three jobs.

This is not theoretical. Before the validation module was wired in, the generated schema marked
nothing as required and the model routinely dropped `categoriaId` and `notificacao`.

### Providers are adapters, chosen by an environment variable

`ChatClient` is the port. `GeminiChatClient` and `AnthropicChatClient` are adapters that only know
how to speak their provider's protocol — the schema, the system prompt and the context assembly
live in the neutral `ContratoLembreteIA`. `IA_PROVEDOR=gemini|anthropic` decides which one becomes
a bean (`@ConditionalOnProperty`); only the chosen one is instantiated.

The route is `/api/v1/chat/lembrete`, deliberately not named after a vendor. It has already changed
providers once, and the URL did not have to change with it.

### The AI quota is a single atomic UPDATE

Every call to a model costs real money, so each anonymous session gets 10. The interesting part is
how the budget is spent:

```java
@Query("update SessaoAnonimaEntity s set s.chamadasIa = s.chamadasIa + 1 "
     + "where s.uuid = :uuid and s.chamadasIa < :limite")
int consumirChamadaIa( UUID uuid, int limite );
```

The increment and the ceiling check happen in the same statement, and the row count says whether
there was room (`1`) or not (`0` → `429`). Read-check-save in two steps would leave a window where
two parallel requests both see nine calls used and both spend the tenth.

The quota is also consumed **before** the provider call, not after: it protects the spend, so the
slot is claimed the moment we decide to spend it.

### The recurrence engine

Three strategies answer *when it fires* — cron (calendar rules), fixed interval, and explicit
dates. Calendar recurrences are compiled into a cron expression by `CronExpressionBuilder`;
weekends and Brazilian national holidays are resolved through a `FeriadoPort`, with a policy per
reminder: `PULAR` (skip), `ADIAR` (move to the next business day) or `ANTECIPAR` (move to the
previous one).

Two design decisions the code alone does not explain:

- **Business limits do not live in the strategies.** An end date (`dataFim`) and a remaining count
  (`quantidade`) apply to all three, so they sit in `Notificacao`. Pushing them down would mean
  writing the same rule three times and maintaining three chances to get it wrong.
- **Scheduling and projection are separate methods on purpose.** The scheduling path computes one
  firing at a time and is allowed to mutate state; the read path (`proximasExecucoes`, the preview
  of the next five) must be pure, because it runs on every serialization. Collapsing them into one
  method — the obvious refactor — would turn a `GET` into a write.

`proximasExecucoes` is therefore **computed, never persisted**: recalculated on every read, honoring
the end date and the remaining quota, so it shortens and disappears as the reminder advances.

### Quartz with a JDBC job store, and what that forces you to handle

The job store is JDBC, not memory. With `RAMJobStore`, every restart silently erased the triggers:
the reminders stayed in the database as rows that would never fire again, with no error anywhere.
Persisting them fixed that, and introduced two problems that memory had been hiding:

**Overdue firings.** Everything that came due while the application was down comes back at once on
boot — Quartz calls it a misfire and, by default, fires it immediately. For a reminder app that is
a flood of stale notifications, and a late notification loses its value fast.

Telling Quartz to discard the misfire is not an option: these triggers fire once and the job itself
schedules the next one, so a discarded trigger means a reminder orphaned forever. So the job runs,
and the filter sits inside it — more than **5 minutes** late and it reschedules **without
notifying**, and without consuming quota, since nothing was delivered. A two-minute delay from a
quick restart still notifies; a day-old one does not. The boundary is pinned by
[`DisparoVencidoTest`](src/test/java/com/routine/pusher/application/job/DisparoVencidoTest.java).

**Orphan triggers.** A trigger whose reminder no longer exists used to fix itself on the next
restart. With a persistent store it fires, finds nothing, logs, and gets rescheduled — forever. It
now has to be deleted explicitly, which is what `LembreteExecutorJob` does before returning.

The schema for the Quartz tables is versioned as a migration (`V2__quartz.sql`) rather than created
by Quartz itself, because a schema has exactly one owner: Flyway.

### Deployed on AWS Lightsail Containers

`sa-east-1`, `nano` plan (512 MB, US$ 7/month), Postgres on Supabase, HTTPS and a custom domain.
The plan was chosen by measurement, not by guess: 273 MB idle, 311 MB under load.

Two deployments failed before one worked, and both are written up in
[`deploy/lightsail/README.md`](deploy/lightsail/README.md) along with the IAM policy, the Supabase
pooler traps and the full procedure. The short version of the second one: a 158-second boot missed
the Lightsail health window by 16 seconds; `-XX:TieredStopAtLevel=1` (C1 only, no C2 compiler)
brought it down to 103s. On 0.25 vCPU the bottleneck is startup, not throughput — if traffic ever
inverts that, it is the first flag to remove.

The multi-stage `Dockerfile` carries nothing platform-specific, which is the point: changing
providers is not a rewrite.

---

## Run locally

### With Docker Compose (everything in one command)

```sh
cp .env.example .env     # optional: only needed for the AI chat
docker compose up --build
```

That builds the image, starts Postgres, waits for it to be genuinely ready, and starts the API
against it. Flyway applies all six migrations on first boot — there are no tables to create by
hand.

| | |
|---|---|
| API | `http://localhost:8080/api/v1` |
| Health | `http://localhost:8080/actuator/health` |
| Postgres | `localhost:5432`, database `routine-pusher` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` — **only under the `dev` profile**, see below |

The API itself needs no credentials, but anything outside `/api/**` and the health check does, and
Swagger UI is in that group: on the default profile it answers `401`. Add
`SPRING_PROFILES_ACTIVE=dev` to the `api` service to browse it.

Compose reads `.env` for interpolation, so a `GEMINI_API_KEY` already sitting there is picked up
automatically. Without one, the chat endpoint reports itself unavailable and everything else works.

**Port already in use?** Both ports are overridable, which matters if you keep an instance running
from your IDE or already have a Postgres container:

```sh
API_PORT=8081 DB_PORT=5433 docker compose up --build
```

Stop with `docker compose down`, or `docker compose down -v` to drop the database volume as well
and start from an empty schema.

### Without Docker

Requires Java 17 and a Postgres reachable on `localhost:5432` with a `routine-pusher` database.

```sh
cp .env.example .env     # DB_PASSWORD is the only variable with no default
./mvnw spring-boot:run
```

> **The `dev` profile** (`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`) puts the whole
> application in `permitAll` and disables CSRF so that Postman, curl and Swagger UI can exercise
> any endpoint. It exists for local testing only — **no security check performed against the `dev`
> profile means anything**, because everything answers 200 there.

A ready-made Postman collection lives in
[`scripts/`](scripts/routine-pusher.postman_collection.json).

---

## Architecture

Three layers, with dependencies always pointing inward. The rule is not a convention: it is pinned
by [`ArchitecturalTest`](src/test/java/com/routine/pusher/infrastructure/arch/ArchitecturalTest.java)
and breaking it fails the build.

```
core/            pure domain — recurrence rules, strategies, enums, ports
  domain/        lembrete, notificacao, recorrencia, categoria, sessao, feriado
  trigger/       when the series ends: end date, count, explicit dates, unlimited
application/     orchestration — services, use cases, controllers, Quartz jobs
infrastructure/  the outside world — security, CORS, Quartz, filters, error handling
```

---

## API

Everything under `/api/v1`. No API route requires a login: the visitor's identity is the anonymous
session carried in a cookie, and every query is scoped by it.

| Method | Route | Purpose |
|---|---|---|
| `POST` | `/lembrete` | create the reminder and schedule its first firing |
| `GET` | `/lembrete?sortInfo=<field>&decrescente=<bool>` | list this session's reminders (**both parameters are required**) |
| `PUT` | `/lembrete/{uuid}` | replace the reminder, **rebuilding the schedule** |
| `PATCH` | `/lembrete/{uuid}/detalhes` | edit title, description and category, **preserving the schedule** |
| `PATCH` | `/lembrete/{uuid}` | mark as done |
| `DELETE` | `/lembrete/{uuid}` | delete the reminder and cancel its trigger — `204` |
| `POST` | `/categoria` | create a category in this session's list |
| `GET` | `/categoria?sortInfo=<field>&decrescente=<bool>` | list |
| `GET` · `PUT` · `DELETE` | `/categoria/{id}` | fetch, update, delete |
| `POST` | `/chat/lembrete` | **create a reminder from a sentence** |
| `GET` | `/sessao` | inspect the current session (creating one if needed) |
| `DELETE` | `/sessao` | the "forget me": drops the session, its reminders and their schedules |
| `GET` | `/notificar/sse` | SSE stream of this session's firings |

Outside `/api/v1`: `GET /` (identification), `/actuator/health`, `/swagger-ui.html`.

Another visitor's id returns **404**, not 403 — including for categories, whose ids are sequential
and therefore guessable.

### Creating a reminder

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

Three mutually exclusive ways to recur:

- **Interval** — `intervaloDias` / `intervaloHoras` / `intervaloMinutos`, summing to at least
  **5 minutes**. `dataInicio` is the **base of the count, not the first firing**: the first firing
  is `dataInicio + interval`.
- **Calendar** — `diasDaSemana`, `diasFixosNoMes` or `posicaoDaSemanaNoMes`, with
  `notificacao.horario` (`HH:mm`). Compiled into a cron expression internally.
- **Explicit dates** — `notificacao.datasEspecificadas`, with no recurrence.

Optional limits: `recorrencia.quantidade` (total firings), `notificacao.dataFim` (end date) and
`recorrencia.politicaDiaUtil` (`PULAR`, `ADIAR`, `ANTECIPAR` when an occurrence lands on a weekend
or a national holiday).

Omitting recurrence **and** explicit dates is rejected with an explanatory `422`: a reminder that
cannot say when it fires is not a reminder.

### Response

```json
{
  "uuid": "9b1f0c2e-5a7d-4f31-8c60-2a1e9d4b7f03",
  "titulo": "Beber água",
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
    "2026-11-23T10:00:00", "2026-11-23T12:00:00", "2026-11-23T14:00:00",
    "2026-11-23T16:00:00", "2026-11-23T18:00:00"
  ]
}
```

`uuid` is the public identity — the table's sequential id never leaves the persistence layer.
`proximaExecucao` is the firing actually registered in Quartz and matches `proximasExecucoes[0]`;
the rest is projection, since scheduling happens one firing at a time.

### Creating a reminder from a sentence

```http
POST /api/v1/chat/lembrete
{ "frase": "me lembra de beber água a cada 2 horas até as 18h", "agora": "2026-11-23T08:00:00" }
```

Returns the same `LembreteOutputDTO` — already created and already scheduled. `agora` is the
caller's local clock, so that "tomorrow at 9" resolves in the user's timezone rather than the
server's.

### Errors

A single shape, from a `@RestControllerAdvice` that extends `ResponseEntityExceptionHandler` — a
catch-all on `Exception` used to turn client mistakes into 500s:

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

`camposInvalidos` appears only on validation failures. `422` for domain rules (impossible schedule,
deleting a category in use), `429` for exhausted session limits, `404` for what is not yours.

---

## Anonymous sessions

Nobody signs up to try a site they found in a post. The identity is created on the first request to
`/api/**`, travels in the **`RP_SESSAO`** cookie (`HttpOnly`, `SameSite=Lax`, `Secure` whenever the
connection is HTTPS), and everything the visitor creates belongs to it.

| | |
|---|---|
| Inactivity window | **48 h** — any call renews it |
| Cookie lifetime | 7 days, deliberately longer: the server decides, not the browser |
| `ultimo_acesso` refresh | at most once per hour, so it does not become one UPDATE per click |
| Cleanup | hourly Quartz job: expired sessions, their reminders and their schedules |
| Reminder cap | **10** per session |
| AI call cap | **10** per session |

Every new session is seeded with two categories — *Importante* and *Urgente* — created at runtime
rather than by a migration, since they depend on a session that only exists while the application
runs. They are ordinary rows: renameable, recolorable, deletable.

**`SameSite=Lax` has an infrastructure consequence:** the front end and the API must share a
registrable domain (`site.com` and `api.site.com`). Across different domains the cookie is not sent
on XHR, and the failure is silent — creating returns 200, listing returns `[]`, because every
request arrives as a brand new visitor.

That is also why CSRF protection is disabled on `/api/**`: a third-party site cannot attach a `Lax`
cookie to a cross-site POST, and the API's GETs are read-only. A CSRF token would only break
Postman and Swagger.

---

## Database and migrations

The schema is versioned with **Flyway**. Hibernate runs in `validate` and **fails loudly at boot**
if the mapping and the database disagree, instead of discovering it at the first INSERT.

```
src/main/resources/db/migration/
  V1__schema_inicial.sql          reminder, notification, recurrence, category
  V2__quartz.sql                  Quartz tables (official 2.3.2 script)
  V3__sessao_anonima.sql          the visitor's identity
  V4__cota_de_ia_por_sessao.sql   AI call counter on the session row
  V5__categorias_do_demo.sql      global category list (superseded by V6)
  V6__categoria_por_sessao.sql    categories now belong to a session
```

Two tables carry a double key (`lembrete`, `sessao_anonima`): a sequential `BIGINT` as the internal
key — B-tree locality and 8-byte foreign keys — and a `uuid` as the business key, which is what the
API exposes and what identifies the job in Quartz.

**V6 destroys no data**: every global category in use becomes a copy owned by the session using it,
reminders are repointed, and only then are the globals removed.

---

## Configuration

Everything through environment variables, with development defaults. Fully commented reference in
[`.env.example`](.env.example).

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | local Postgres | on a managed provider, use the pooler endpoint |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / — | the password is the only variable with no default |
| `DB_POOL_SIZE` | `10` | Supabase free tier: `5` |
| `JPA_DDL_AUTO` | `validate` | never `update` with real data |
| `JPA_SHOW_SQL` | `false` | development machine only |
| `FLYWAY_ENABLED` | `true` | disabled only in tests |
| `QUARTZ_THREADS` | `5` | |
| `QUARTZ_CLUSTERED` | `false` | turn on **together with** the second instance, never before or after |
| `IA_PROVEDOR` | `gemini` | `gemini` or `anthropic`; only the chosen one becomes a bean |
| `GEMINI_API_KEY` / `GEMINI_MODEL` | — / `gemini-3.5-flash-lite` | free key at AI Studio, no card required |
| `ANTHROPIC_API_KEY` / `ANTHROPIC_MODEL` | — / `claude-haiku-4-5` | requires API credit |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | exact origins, comma-separated; no wildcard |
| `PORT` | `8080` | injected by the platform |

Model names leave provider catalogs without notice — `gemini-2.5-flash-lite` stopped being served
to new accounts mid-project — which is exactly why the model is a variable and not a constant.

---

## Tests

```sh
./mvnw test
```

149 tests on H2 (`create-drop`, Flyway off — the migrations are Postgres SQL), covering the
recurrence engine, business days and holidays, timezones, session scoping, both AI providers'
schema dialects, the public routes and the architecture rules.

> **A green suite says nothing about the production `application.yml`.** The file at
> `src/test/resources/application.yml` **shadows** the main one: the real YAML can be broken with
> the whole suite passing. What catches that is running the image:
>
> ```sh
> docker run --rm --memory=512m -p 8080:8080 --env-file .env.production routine-pusher:latest
> ```

---

## Known limitations

Scope read as scope, not as oversights:

- **Notifications only arrive with the tab open.** The channel is SSE. Web Push would require a
  service worker and a persistent identity, both deliberately refused in an anonymous demo.
- **Reminders carry a time, not a timezone.** The domain works in `LocalDateTime` and the zone is
  pinned in the container. The real fix is storing the creator's zone per reminder.
- **The AI quota can be bypassed by clearing the cookie.** On Gemini's free tier the provider's
  daily ceiling caps the damage at "the chat stops for today"; a paid provider would need a
  per-IP limit first.
- **48-hour retention.** A reminder set two weeks out does not survive a visitor who does not come
  back within two days. Real retention needs accounts, not anonymous sessions.
- **Nothing stops a visitor from deleting every category**, and without one no reminder can be
  created. The front end is what prevents it.
- **A single instance.** With two and `QUARTZ_CLUSTERED=false`, both would fire the same reminder.
- **No CI.** Deployment is manual, from the development machine.
