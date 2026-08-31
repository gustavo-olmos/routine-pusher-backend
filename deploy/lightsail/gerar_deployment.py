"""Gera o arquivo de deployment do Lightsail a partir do .env local.

Fica separado do deploy.sh de propósito: assim dá para exercitar a substituição sem docker, sem
AWS e sem publicar nada — que é justamente a parte onde um erro passa despercebido até virar um
container reiniciando em produção.

Uso:  python gerar_deployment.py <.env> <template.json> <destino.json> [versao-da-imagem]
"""
import io
import json
import re
import sys

# Sem estas a aplicação sobe e falha: banco inacessível ou chat de IA morto.
#
# DB_USERNAME entra na lista porque vazio é pior que ausente: o template emitiria "" e a variável
# de ambiente vence o default `${DB_USERNAME:postgres}` do application.yml. No pooler do Supabase o
# usuário é `postgres.<ref>`, e o sintoma de errar isso é um enganoso
# `password authentication failed for user "postgres"` — que aponta para a senha, não para o usuário.
OBRIGATORIAS = ("DB_URL", "DB_USERNAME", "DB_PASSWORD", "GEMINI_API_KEY")

# O que o .env de desenvolvimento não precisa declarar, mas o deploy precisa ter.
PADROES = {
    "DB_POOL_SIZE": "5",   # free tier do Supabase tem orçamento pequeno de conexões
    "IA_PROVEDOR": "gemini",
    "CORS_ALLOWED_ORIGINS": "",
}


def ler_env(caminho):
    valores = dict(PADROES)
    for linha in io.open(caminho, encoding="utf-8"):
        linha = linha.strip()
        if not linha or linha.startswith("#") or "=" not in linha:
            continue
        chave, _, valor = linha.partition("=")
        valor = valor.strip()
        if valor:
            valores[chave.strip()] = valor
    return valores


def gerar(env_file, template, destino, versao=None):
    valores = ler_env(env_file)
    texto = io.open(template, encoding="utf-8").read()
    vazias = []

    def substituir(achado):
        chave = achado.group(1)
        valor = valores.get(chave, "")
        if not valor:
            vazias.append(chave)
        # A senha do Supabase costuma trazer caractere especial: escapar aqui evita gerar um
        # JSON quebrado que só apareceria como erro obscuro na API da AWS.
        return json.dumps(valor)[1:-1]

    texto = re.sub(r"\$\{([A-Z_]+)\}", substituir, texto)

    if versao:
        texto = texto.replace(":routine-pusher.api.__VERSAO_IMAGEM__", versao)

    criticas = sorted(set(OBRIGATORIAS).intersection(vazias))
    if criticas:
        raise SystemExit("ERRO: sem valor no .env para: " + ", ".join(criticas))

    if "CORS_ALLOWED_ORIGINS" in vazias:
        print("    AVISO: CORS_ALLOWED_ORIGINS vazio — o navegador vai bloquear o front "
              "hospedado em outro domínio")

    # Falha aqui, e não na AWS: JSON inválido enviado ao Lightsail vira mensagem enigmática.
    json.loads(texto)

    io.open(destino, "w", encoding="utf-8", newline="").write(texto)
    print("    arquivo gerado (conteúdo não exibido: carrega segredos)")


if __name__ == "__main__":
    if len(sys.argv) < 4:
        raise SystemExit(__doc__)
    gerar(*sys.argv[1:5])
