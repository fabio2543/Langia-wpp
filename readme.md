# 🚀 LangIA API – Sprint 1: Webhook de Recebimento (IN/RECEIVED)

## 🧩 Visão Geral

O projeto **LangIA** é um sistema de apoio ao aprendizado de idiomas, no qual a interação inicial com o aluno ocorre via **WhatsApp**.  
Nesta primeira sprint, foi desenvolvido o **módulo de recepção de mensagens (Webhook)**, que valida e armazena as mensagens recebidas do **WhatsApp Cloud API**.

---

## 🏗️ Objetivo da Sprint

Implementar o canal de recepção de mensagens do WhatsApp Cloud API, responsável por:

- Validar a origem das mensagens por meio de **HMAC (X-Hub-Signature-256)**.  
- Gravar as mensagens recebidas no banco **PostgreSQL (JSONB)**.  
- Preparar o pipeline para evolução do aprendizado do aluno.  

---

## ⚙️ Stack Técnica

| Componente | Versão / Tecnologia |
|-------------|--------------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build | Maven |
| Banco de Dados | PostgreSQL 15 (Docker) |
| ORM | Hibernate 6 + JSONB |
| Segurança | Spring Security (permitAll, CSRF ignore) |
| Logging | SLF4J + Logback |
| Utilitários | Lombok, ObjectMapper |
| Infraestrutura | Docker Compose |

---

## 🗂️ Estrutura do Projeto

```
src/main/java/com/langia/
├── config/WebSecurityPermitWebhook.java
├── controller/WebhookController.java
├── entity/MessageLog.java
├── repository/MessageLogRepository.java
├── service/whatsapp/
│   ├── SignatureVerifier.java
│   └── WebhookProcessor.java
└── LangiaApplication.java
```

---

## 🔐 Variáveis de Ambiente

| Variável | Descrição | Exemplo |
|-----------|------------|----------|
| `SERVER_PORT` | Porta HTTP da aplicação | `8080` |
| `WHATSAPP_VERIFY_TOKEN` | Token de verificação usado pelo GET do webhook | `dev-verify` |
| `WHATSAPP_APP_SECRET` | Segredo usado na assinatura HMAC | `dev-secret` |
| `SPRING_DATASOURCE_URL` | URL do banco Postgres | `jdbc:postgresql://pg:5432/langia` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `langia` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `langia` |

---

## 🐳 Execução via Docker

### 1️⃣ Subir containers
```bash
docker compose up -d --build
```

### 2️⃣ Verificar logs
```bash
docker logs -f langia-api | egrep -i 'Tomcat started|Started|Webhook'
```

### 3️⃣ Acessar API
- API: [http://localhost:8080](http://localhost:8080)
- Banco: `localhost:5432`

---

## 🧠 Endpoints Implementados

### ✅ GET `/webhooks/whatsapp`
Validação inicial do webhook com o **WhatsApp Cloud API**.  
Retorna o valor do parâmetro `hub.challenge` quando o token enviado (`hub.verify_token`) corresponde ao definido na variável `WHATSAPP_VERIFY_TOKEN`.

#### Exemplo de teste:
```bash
curl -G "http://localhost:8080/webhooks/whatsapp"   --data-urlencode "hub.mode=subscribe"   --data-urlencode "hub.verify_token=dev-verify"   --data-urlencode "hub.challenge=123456"
```
🟢 **Resposta esperada:**  
```
123456
```

---

### ✅ POST `/webhooks/whatsapp`
Recebe mensagens enviadas pelo WhatsApp Cloud API.  
O corpo da requisição é validado via assinatura HMAC (`X-Hub-Signature-256`).  
Em caso de sucesso, o payload é armazenado como JSONB na tabela `message_log`.

#### Exemplo de requisição:
```bash
BODY='{"entry":[{"changes":[{"value":{"messages":[{"from":"+5511999999999","id":"wamid.TEST123","timestamp":"1730900000","text":{"body":"Olá LangIA!"},"type":"text"}]}}]}]}'
SECRET='dev-secret'
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$SECRET" -binary | xxd -p -c 256)

curl -i -H "Content-Type: application/json; charset=utf-8"      -H "X-Hub-Signature-256: sha256=$SIG"      --data-binary "$BODY"      http://localhost:8080/webhooks/whatsapp
```

🟢 **Resposta esperada:**
```
HTTP/1.1 200 OK
```

---

## 🗄️ Estrutura da Tabela `message_log`

| Coluna | Tipo | Descrição |
|---------|------|-----------|
| `id` | BIGSERIAL (PK) | Identificador único |
| `direction` | VARCHAR | Direção da mensagem (`IN` / `OUT`) |
| `student_id` | BIGINT | ID do aluno associado (opcional) |
| `payload` | JSONB | Conteúdo bruto da mensagem |
| `status` | VARCHAR | RECEIVED / SENT / ERROR |

---

## 🧪 Testes SQL

### Consultar últimas mensagens
```sql
SELECT id, direction, status, jsonb_typeof(payload) AS tipo, jsonb_pretty(payload)
FROM message_log
ORDER BY id DESC
LIMIT 5;
```

### Consultar apenas mensagens recebidas
```sql
SELECT id, payload->'entry'->0->'changes'->0->'value'->'messages'->0->'text'->>'body' AS mensagem
FROM message_log
WHERE direction = 'IN'
ORDER BY id DESC
LIMIT 5;
```

---

## 🧾 Resultados da Sprint

- ✅ Webhook configurado e validado via GET/POST.  
- ✅ HMAC verificado com sucesso.  
- ✅ Persistência JSONB em banco PostgreSQL confirmada.  
- ✅ Logs detalhados e retorno HTTP 200.  
- ⚙️ Ambiente Docker funcional e isolado.

---

## 🧭 Próximos Passos (Sprint 2)

| Módulo | Descrição |
|---------|------------|
| H1.2 – Envio de Mensagens (OUT/SENT) | Implementar integração com WhatsApp Cloud API para envio. |
| H1.3 – Callback de Entrega | Atualizar status de entrega e leitura (DELIVERED, READ). |
| V2 – Auditoria | Criar colunas `created_at`, `updated_at` e índices. |


---

## 🧰 Repositório e Autoria

**Autor:** José Fábio Júnior  
**Projeto:** LangIA (2025)  
**Repositório:** [https://github.com/fabio2543/langia-wpp](https://github.com/fabio2543/langia-wpp)

---

> _Sprint 1 entregue com sucesso – módulo de recepção de mensagens WhatsApp validado, seguro e integrado ao pipeline do LangIA._

⚙️ 1. Subir o ambiente completo (API + Banco)
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d --build


🔹 Esse comando:

Constrói a imagem do Spring Boot (compila o JAR localmente).

Cria os containers:

langia-pg-dev → banco PostgreSQL

langia-api-dev → aplicação Spring Boot

Usa as variáveis definidas no arquivo .env.dev.

🔍 2. Verificar se os containers estão rodando
docker ps


Exemplo esperado:

NAME             STATUS                    PORTS
langia-api-dev   Up (healthy)              0.0.0.0:8081->8080/tcp
langia-pg-dev    Up (healthy)              0.0.0.0:5433->5432/tcp


🧾 4. Ver logs da aplicação
docker compose -f docker-compose.dev.yml --env-file .env.dev logs -f api-dev

🧠 5. Acessar o container manualmente (debug opcional)
docker exec -it langia-api-dev /bin/bash


Dentro do container, é possível rodar o app manualmente:

java -jar /app/langia-api.jar

🔁 6. Rebuild rápido da aplicação (sem recriar tudo)

Após alterações no código:

./mvnw clean package -DskipTests
docker compose -f docker-compose.dev.yml build api-dev
docker compose -f docker-compose.dev.yml up -d api-dev

🧹 7. Zerar o ambiente de desenvolvimento

Parar e remover containers (mantém o banco):

docker compose -f docker-compose.dev.yml --env-file .env.dev down


Apagar tudo, incluindo o banco (volume):

docker compose -f docker-compose.dev.yml --env-file .env.dev down -v


Reiniciar do zero:

docker compose -f docker-compose.dev.yml --env-file .env.dev up -d --build