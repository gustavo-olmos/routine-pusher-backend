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

A API conta com autenticação e autorização para proteger os endpoints. Em breve, será detalhado o processo de autenticação via JWT.

## Contribuição

Se desejar contribuir, siga os passos:

1. Faça um fork do repositório
2. Crie uma branch (`feature/nova-funcionalidade`)
3. Envie um pull request

---

Projeto em desenvolvimento 🚀

