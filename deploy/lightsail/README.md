# Deploy no AWS Lightsail Containers

Publica a API como container gerenciado, com HTTPS incluso, por US$ 7/mês no plano `nano`.

Medido antes de escolher o plano: a aplicação usa **273 MB parada e 311 MB sob carga** com limite de
512 MB, então o `nano` basta — o `micro` (1 GB) seria dinheiro parado.

## Antes de começar

**1. Banco no Supabase.** Crie o projeto e pegue a string em *Project Settings → Database →
Connection pooling*. Duas armadilhas que custam uma tarde cada:

- Use o endpoint do **pooler** (Supavisor), não a conexão direta: a direta é IPv6 em projetos novos
  e a maioria dos hosts não tem saída IPv6.
- O pooler em *transaction mode* quebra o prepared statement do driver: a URL precisa terminar com
  `?prepareThreshold=0`.

Não é preciso criar tabela nenhuma: o Flyway cria o schema inteiro no primeiro boot (5 migrações,
incluindo as tabelas do Quartz e as categorias do demo).

**2. Chave do Gemini.** Gratuita em [aistudio.google.com](https://aistudio.google.com) → *Get API
key*, sem cartão.

**3. AWS CLI.** `aws configure` com uma credencial que tenha permissão de Lightsail.

**4. Arquivo de configuração:**

```bash
cp .env.production.example .env.production   # já ignorado pelo git
```

Preencha `DB_URL`, `DB_PASSWORD`, `GEMINI_API_KEY` e `CORS_ALLOWED_ORIGINS`. O script recusa a rodar
sem as três primeiras — de propósito, e antes de gastar minutos construindo a imagem.

## Publicando

```bash
# uma vez, para criar o serviço
aws lightsail create-container-service \
    --region sa-east-1 --service-name routine-pusher --power nano --scale 1

# a cada deploy
./deploy/lightsail/deploy.sh
```

O script constrói a imagem, envia ao Lightsail e cria o deployment. O arquivo que carrega os
segredos é gerado na hora e apagado no fim, inclusive se algo falhar no meio.

A URL sai em:

```bash
aws lightsail get-container-services --region sa-east-1 --service-name routine-pusher
```

O primeiro deploy leva alguns minutos: o Lightsail só desvia o tráfego depois que
`/actuator/health` responder 200 duas vezes seguidas.

## Decisões que já estão embutidas, e por quê

**Região `sa-east-1` (São Paulo).** Latência para o público brasileiro, e coerência com o fuso
fixado na imagem.

**Health check em `/actuator/health`, não em `/`.** O padrão do Lightsail é a raiz. A raiz hoje
responde 200 (`RaizController`), mas `/actuator/health` é o que de fato consulta o banco — é a
diferença entre "o processo está vivo" e "a aplicação funciona".

**Fuso fixado em `America/Sao_Paulo` no Dockerfile.** O agendamento usa hora sem fuso, então o
relógio do servidor precisa ser o do público. Container roda em UTC por padrão, o que faria todo
lembrete das 9h disparar às 6h. É mitigação, não correção: a correção é cada lembrete guardar o
fuso de quem o criou.

**`DB_POOL_SIZE=5`.** O free tier do Supabase cobra conexão, e o pooler dele já divide o orçamento.

**Uma instância só (`--scale 1`).** Com duas, o Quartz precisa de `QUARTZ_CLUSTERED=true`, senão as
duas disparam o mesmo lembrete. A variável existe; ligue-a **junto** com a segunda instância.

## Depois que subir

- **CORS**: `CORS_ALLOWED_ORIGINS` precisa ser a origem exata do front (esquema + host).
- **Domínio**: a API deve ficar num subdomínio do **mesmo domínio registrável** do front (ex.:
  `api.seudominio.com` e `seudominio.com`). O cookie de sessão é `SameSite=Lax` e não viaja entre
  domínios diferentes — sem isso, cada requisição do front vira um visitante novo.
- **Login do Google** é opcional: sem `GOOGLE_CLIENT_ID`/`SECRET` a aplicação sobe e a API do demo
  funciona inteira. Configure só se quiser o Swagger acessível ou administrar categorias pela API.

## O que ainda não está resolvido

- **Cota de IA por sessão é burlável** limpando o cookie. No free tier do Gemini o teto diário do
  Google limita o prejuízo a "o chat para no dia"; se trocar para um provedor pago, ponha um limite
  por IP antes.
- **Sem CI**: o deploy é manual, a partir da sua máquina.
