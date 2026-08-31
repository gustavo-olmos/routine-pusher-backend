# Deploy no AWS Lightsail Containers

Publica a API como container gerenciado, com HTTPS incluso, por US$ 7/mês no plano `nano`.

Medido antes de escolher o plano: a aplicação usa **273 MB parada e 311 MB sob carga** com limite de
512 MB, então o `nano` basta — o `micro` (1 GB) seria dinheiro parado.

## Antes de começar

**1. Banco no Supabase.** Crie o projeto e pegue a string em *Project Settings → Database →
Connection pooling*. Duas armadilhas que custam uma tarde cada:

- Use o endpoint do **pooler** (Supavisor), não a conexão direta: a direta é IPv6 em projetos novos
  e a maioria dos hosts não tem saída IPv6.
- Prefira a **porta 5432 do pooler** (*session mode*): os prepared statements funcionam normalmente
  e os travamentos de linha do Quartz se comportam sem surpresa. A porta 6543 é *transaction mode* e
  aí a URL precisa terminar com `?prepareThreshold=0` — mais peça móvel para o mesmo resultado.
- **Escolha a região South America (São Paulo).** Medido em 2026-08-30: o pooler em `us-west-2`
  responde a 262 ms contra 69 ms do `sa-east-1`. Como cada requisição faz várias consultas (resolver
  a sessão, renovar, listar) e o Quartz consulta o banco continuamente, isso multiplica — e
  contradiz a escolha de rodar o container em São Paulo.

Não é preciso criar tabela nenhuma: o Flyway cria o schema inteiro no primeiro boot (5 migrações,
incluindo as tabelas do Quartz e as categorias do demo). **Verificado em 2026-08-30** rodando a
aplicação local contra o Supabase: as 5 migrações aplicaram e o chat de IA criou lembrete no
banco remoto.

> Ao testar assim, passe as credenciais como **variáveis de ambiente**, não via
> `--spring.config.import`: o `.env` local continua sendo importado pelo `application.yml` e
> vence na precedência, e o sintoma é um confuso `password authentication failed for user
> "postgres"` — com o usuário errado, não a senha.

**2. Chave do Gemini.** Gratuita em [aistudio.google.com](https://aistudio.google.com) → *Get API
key*, sem cartão.

**3. AWS CLI v2 + plugin lightsailctl — instale DENTRO da WSL, não no Windows.**

Este é o ponto que mais custa tempo se errado. O `push-container-image` lê a imagem do **daemon do
Docker local**, e nesta máquina o Docker existe apenas dentro da WSL — não há daemon do lado
Windows. Uma AWS CLI instalada no Windows não enxergaria imagem nenhuma para enviar.

Então tudo mora no mesmo lugar: Docker, CLI e plugin, todos na WSL. E o deploy é rodado de lá.

```bash
wsl -d Ubuntu                       # a partir daqui, tudo dentro da WSL

sudo apt update && sudo apt install -y unzip      # o instalador vem em .zip e o Ubuntu não traz unzip

curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscli.zip
unzip -q /tmp/awscli.zip -d /tmp && sudo /tmp/aws/install
aws configure                                     # ver "Permissões" logo abaixo

# plugin Lightsail Control (é uma instalação separada da CLI)
curl -fsSL "https://s3.us-west-2.amazonaws.com/lightsailctl/latest/linux-amd64/lightsailctl"      -o /tmp/lightsailctl
sudo mv /tmp/lightsailctl /usr/local/bin/lightsailctl && sudo chmod +x /usr/local/bin/lightsailctl
```

Sem o plugin, o erro é um lacônico "plugin not found" no meio do deploy — ele é quem sabe ler a
imagem do Docker local. Versões e instruções por sistema operacional na
[documentação oficial](https://docs.aws.amazon.com/lightsail/latest/userguide/amazon-lightsail-install-software.html).

Dá para viver sem CLI e plugin, mas o custo é alto: como nada no navegador consegue ler uma imagem
da sua máquina, o caminho pelo console exige publicar a imagem antes num registro público (Docker
Hub) e depois digitar as variáveis de ambiente — senha do banco e chave da IA incluídas — num
formulário web a cada revisão.

**Permissões do usuário IAM.** Não existe política gerenciada de acesso total ao Lightsail — a
única que a AWS publica é a `LightsailExportAccess`, que serve para exportar snapshots e não dá o
que precisamos. A política é sua, e o conteúdo está em [`politica-iam.json`](politica-iam.json).

Faça a **política antes do usuário**: o assistente de criação de usuário só oferece anexar políticas
já existentes, então na ordem inversa o usuário nasce sem permissão e você tem que voltar nele.

1. *IAM → Policies → Create policy → aba JSON* → cole o arquivo → nomeie (ex.:
   `LightsailDeployRoutinePusher`).
2. *IAM → Users → Create user* → o acesso ao console pode ficar desmarcado (é usuário só de CLI) →
   *Attach policies directly* → marque a política recém-criada.
3. *Security credentials → Create access key → Command Line Interface (CLI)* → baixe o `.csv`. A
   secret aparece **uma única vez**.

`lightsail:*` cobre criar o serviço, registrar a imagem e publicar o deployment. É amplo, mas ainda
muito mais estreito que `AdministratorAccess`, e restrito a um serviço só. O `Resource: "*"` é
necessário: o Lightsail não suporta permissão por recurso na maioria das ações.

**4. Arquivo de configuração:**

```bash
cp .env.production.example .env.production   # já ignorado pelo git
```

Preencha `DB_URL`, `DB_PASSWORD`, `GEMINI_API_KEY` e `CORS_ALLOWED_ORIGINS`. O script recusa a rodar
sem as três primeiras — de propósito, e antes de gastar minutos construindo a imagem.

## Publicando

Rode **de dentro da WSL** (`wsl -d Ubuntu`), onde vivem o Docker, a CLI e o plugin:

```bash
cd /mnt/c/Users/guolm/Dev/Projetos/Back/routine-pusher-backend

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

**`unhealthyThreshold` em 10, não no 3 do padrão.** O `nano` tem 0,25 vCPU e a JVM sobe bem mais
devagar que numa máquina de desenvolvimento. Com 3 tentativas de 30s, um boot de 95s faria o
Lightsail reverter um deployment perfeitamente saudável. Agora são 300s de tolerância. Medido: 30s
para `/actuator/health` responder UP num container local com 512 MB — o teto existe para a margem
do hardware compartilhado, não porque o boot seja lento.

## Depois que subir

- **CORS**: `CORS_ALLOWED_ORIGINS` precisa ser a origem exata do front (esquema + host).
- **Domínio**: a API deve ficar num subdomínio do **mesmo domínio registrável** do front (ex.:
  `api.seudominio.com` e `seudominio.com`). O cookie de sessão é `SameSite=Lax` e não viaja entre
  domínios diferentes — sem isso, cada requisição do front vira um visitante novo.
- **Login do Google** é opcional: sem `GOOGLE_CLIENT_ID`/`SECRET` a aplicação sobe e a API do demo
  funciona inteira. Configure só se quiser o Swagger acessível ou administrar categorias pela API.

  Isto já foi falso e derrubou o primeiro deploy. A registração vivia em
  `spring.security.oauth2.client.registration.google` com default vazio, na crença de que valor
  vazio desligava o login — o Spring Boot valida as registrações **declaradas** e recusa
  `client-id` vazio, então a chave no YAML bastava para matar o boot. Hoje a registração está em
  `app.google.*` e só nasce com credencial (`GoogleOAuthConfig`).

  **Verifique isso rodando a imagem, não a suíte de testes:** `src/test/resources/application.yml`
  sombreia o principal, então nenhum teste enxerga o YAML que vai a produção.

  ```bash
  docker run --rm --memory=512m -p 8080:8080 --env-file .env.production routine-pusher:latest
  ```

  O `.env.production` não tem variável do Google, e o `--memory` reproduz o limite do `nano`. Se
  subir aqui e responder UP em `/actuator/health`, sobe no Lightsail.

## O que ainda não está resolvido

- **Cota de IA por sessão é burlável** limpando o cookie. No free tier do Gemini o teto diário do
  Google limita o prejuízo a "o chat para no dia"; se trocar para um provedor pago, ponha um limite
  por IP antes.
- **Sem CI**: o deploy é manual, a partir da sua máquina.
