# Multi-stage: a imagem final não carrega Maven, código-fonte nem JDK completo.
#
# Serve para Railway, Render, Fly.io e AWS App Runner sem alteração — é justamente o ponto de manter
# o build aqui e não num arquivo de configuração de plataforma: trocar de provedor não vira
# reescrita.

# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# pom primeiro, sozinho: enquanto as dependências não mudarem, o Docker reaproveita esta camada e
# não baixa o repositório inteiro a cada alteração de código.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- runtime ----------
# 17 porque e o que o pom declara em <java.version>. Trocar aqui sem trocar la
# significaria rodar em uma JVM diferente da que compilou e testou.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário sem privilégio: um processo que só precisa abrir uma porta não tem por que ser root.
RUN addgroup -S routine && adduser -S routine -G routine
USER routine

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage porque o default da JVM em container pequeno reserva pouco heap e sobra memória
# ociosa. O container é a unidade de memória, então o heap acompanha o limite dele.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

# Forma shell de propósito, para $JAVA_OPTS e $PORT serem expandidos.
# O Spring lê PORT via server.port; a plataforma injeta essa variável.
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
