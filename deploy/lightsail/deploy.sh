#!/usr/bin/env bash
#
# Publica a API no Lightsail Containers.
#
# O arquivo de deployment carrega segredos (senha do banco, chave da IA), então ele NÃO é
# versionado: nasce a cada execução a partir do .env local e é apagado no fim. O que fica no
# repositório é só o template com placeholders.
#
# Uso:  ./deploy/lightsail/deploy.sh [nome-do-servico]
#
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICO="${1:-routine-pusher}"
REGIAO="${AWS_REGION:-sa-east-1}"
ROTULO="api"

AQUI="$RAIZ/deploy/lightsail"
TEMPLATE="$AQUI/containers.template.json"
ENDPOINT="$AQUI/public-endpoint.json"
GERADO="$AQUI/containers.json"
ENV_FILE="${ENV_FILE:-$RAIZ/.env.production}"

# O arquivo gerado some mesmo se algo falhar no meio: segredo em disco é segredo esquecido.
trap 'rm -f "$GERADO"' EXIT

# Arquivo separado do .env de propósito: o local aponta para o Postgres da máquina, o de
# produção para o Supabase. Um só arquivo faria o desenvolvimento escrever no banco do demo.
[ -f "$ENV_FILE" ] || {
  echo "ERRO: $ENV_FILE não encontrado — copie de .env.production.example e preencha"
  exit 1
}

# --- 0. interpretador -----------------------------------------------------------------------
# Ubuntu traz só `python3`; o Git Bash do Windows traz só `python`. Testar execução de verdade, e
# não apenas a existência do comando, porque no Windows `python3` pode ser um atalho da Store que
# não roda nada.
PY=""
for candidato in python3 python; do
  if command -v "$candidato" >/dev/null 2>&1 && "$candidato" -c "pass" >/dev/null 2>&1; then
    PY="$candidato"; break
  fi
done
[ -n "$PY" ] || { echo "ERRO: nenhum Python utilizável (procurei python3 e python)"; exit 1; }

# --- 1. configuração ------------------------------------------------------------------------
# Antes do build de propósito: falta de variável é erro de segundos, build é erro de minutos.
echo "==> conferindo o .env e montando o deployment"
"$PY" "$AQUI/gerar_deployment.py" "$ENV_FILE" "$TEMPLATE" "$GERADO"

# --- 2. imagem ------------------------------------------------------------------------------
# Docker pode estar só dentro da WSL (caso desta máquina); a chamada se adapta.
if docker info >/dev/null 2>&1; then
  DOCKER="docker"
elif wsl.exe -d Ubuntu -- docker info >/dev/null 2>&1; then
  DOCKER="wsl.exe -d Ubuntu -- docker"
else
  echo "ERRO: docker não encontrado nem no host nem na WSL"; exit 1
fi

echo "==> construindo a imagem"
( cd "$RAIZ" && $DOCKER build -t "$SERVICO:latest" . )

# --- 3. envio ao Lightsail ------------------------------------------------------------------
if ! command -v aws >/dev/null 2>&1; then
  cat <<AVISO

A imagem está pronta, mas a AWS CLI não foi encontrada. Instale-a e rode de novo, ou execute à mão:

  aws lightsail push-container-image \\
      --region $REGIAO --service-name $SERVICO --label $ROTULO --image $SERVICO:latest

  # o push responde com a referência da imagem (ex.: :$SERVICO.$ROTULO.7); use-a em:
  aws lightsail create-container-service-deployment \\
      --region $REGIAO --service-name $SERVICO \\
      --containers file://$GERADO --public-endpoint file://$ENDPOINT

AVISO
  exit 0
fi

echo "==> enviando a imagem"
SAIDA=$(aws lightsail push-container-image \
          --region "$REGIAO" --service-name "$SERVICO" --label "$ROTULO" \
          --image "$SERVICO:latest")
echo "$SAIDA"

# O push devolve a referência interna da imagem; lê-la daqui evita ter que editar o template.
VERSAO=$(echo "$SAIDA" | grep -oE ":$SERVICO\.$ROTULO\.[0-9]+" | tail -1)
[ -n "$VERSAO" ] || { echo "ERRO: não consegui ler a versão da imagem na saída do push"; exit 1; }

echo "==> apontando o deployment para $VERSAO"
"$PY" "$AQUI/gerar_deployment.py" "$ENV_FILE" "$TEMPLATE" "$GERADO" "$VERSAO"

# --- 4. publicação --------------------------------------------------------------------------
echo "==> publicando"
aws lightsail create-container-service-deployment \
    --region "$REGIAO" --service-name "$SERVICO" \
    --containers "file://$GERADO" \
    --public-endpoint "file://$ENDPOINT"

cat <<FIM

Publicado. Acompanhe com:
  aws lightsail get-container-services --region $REGIAO --service-name $SERVICO

A URL aparece no campo 'url'. O primeiro deploy leva alguns minutos: o Lightsail só desvia o
tráfego depois que o health check em /actuator/health passar duas vezes seguidas.
FIM
