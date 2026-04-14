# Java Enterprise Boilerplate

> Spring Boot 3.x + Multi-Tenant + JWT + Kafka + Redis — production-ready desde o primeiro commit.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql)](https://postgresql.org)
[![Kafka](https://img.shields.io/badge/Kafka-3.x-231F20?logo=apachekafka)](https://kafka.apache.org)
[![Redis 7](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)](https://redis.io)

---

## Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura](#arquitetura)
3. [Pré-requisitos](#pré-requisitos)
4. [Como Rodar Localmente](#como-rodar-localmente)
5. [Módulos](#módulos)
6. [Autenticação e RBAC](#autenticação-e-rbac)
7. [Multi-tenancy](#multi-tenancy)
8. [Como Criar um Novo Tenant](#como-criar-um-novo-tenant)
9. [Como Adicionar um Novo Módulo](#como-adicionar-um-novo-módulo)
10. [Mensageria Kafka](#mensageria-kafka)
11. [Cache Redis](#cache-redis)
12. [Observabilidade](#observabilidade)
13. [OpenAPI / Swagger](#openapi--swagger)
14. [Deploy](#deploy)
15. [Qualidade de Código](#qualidade-de-código)

---

## Visão Geral

Este boilerplate encapsula decisões de arquitetura enterprise para aplicações Java que precisam suportar:

- **Multi-tenancy** com isolamento por schema PostgreSQL
- **Autenticação stateless** via JWT (access + refresh token)
- **RBAC fino** com permissões no formato `RESOURCE:ACTION`
- **Mensageria confiável** via Kafka com Outbox Pattern
- **Cache distribuído** com Redis
- **Observabilidade** com Prometheus, Actuator e logs JSON estruturados

---

## Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**, organizado como **Maven multi-module**:

```
java-boilerplate/
├── core/           ← Domínio puro (entidades, exceções, ports)
├── application/    ← Use-cases (orquestração de domínio)
├── infrastructure/ ← Adapters de saída (JPA, Redis, Kafka, JWT)
├── web/            ← Adapter de entrada (REST controllers, filtros, DTOs)
├── bootstrap/      ← Ponto de entrada Spring Boot, configurações globais
├── docker/         ← Dockerfile + docker-compose
└── k8s/            ← Manifests Kubernetes
```

### Regra de Dependência

```
Bootstrap
   ├── Web → Core, Application
   ├── Infrastructure → Core, Application
   └── Application → Core
       └── Core (nenhuma dependência externa)
```

> A regra é verificada automaticamente em build pelo **ArchUnit** — violações falham o CI.

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9 |
| Docker + Docker Compose | 24.x |
| PostgreSQL | 16 (via Docker) |
| Redis | 7 (via Docker) |
| Kafka | 3.x (via Docker) |

---

## Como Rodar Localmente

### 1. Clone e configure o ambiente

```bash
git clone <url>
cd java-boilerplate
cp .env.example .env
# Edite .env com seus valores (JWT_SECRET deve ter ≥ 32 chars)
```

### 2. Suba a infraestrutura com Docker Compose

```bash
docker compose -f docker/docker-compose.yml up -d
```

Isso inicia PostgreSQL, Redis, Kafka e Zookeeper.

### 3. Execute a aplicação

```bash
./mvnw spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local
```

A aplicação estará disponível em `http://localhost:8080`.

### 4. Acesse o Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 5. Teste o login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@acme.com","password":"secret","tenantId":"acme"}'
```

---

## Módulos

| Módulo | Responsabilidade |
|---|---|
| `core` | Entidades de domínio `User`, `Role`, `Permission`, `Tenant`; hierarquia de exceções; interfaces de ports |
| `application` | Implementações de use-cases: `LoginService`, `RefreshTokenService`, `LogoutService` |
| `infrastructure` | JPA entities + repositories, `TenantAwareDataSource`, JWT, Redis, Kafka, Outbox |
| `web` | `AuthController`, DTOs, `MdcFilter`, `TenantResolutionFilter`, `GlobalExceptionHandler` |
| `bootstrap` | `BoilerplateApplication`, `SecurityConfig`, `RedisConfig`, `KafkaConfig`, `OpenApiConfig` |

---

## Autenticação e RBAC

### Fluxo JWT

```
POST /auth/login  →  { access_token (15min), refresh_token (7d) }
POST /auth/refresh  →  { novo access_token, refresh_token rotacionado }
POST /auth/logout  →  invalida refresh_token no Redis
```

### Permissões

Permissões seguem o formato `RESOURCE:ACTION`:

```java
// No controller ou service:
@RequiresPermission("bet:place")
public void placeBet(PlaceBetCommand cmd) { ... }

// Equivalente a:
@PreAuthorize("hasAuthority('PERMISSION_bet:place')")
```

### Hierarquia de Roles

```
ADMIN ──(herda de)──▶ OPERATOR
```

Ao atribuir `ADMIN` a um usuário, ele recebe todas as permissões de `OPERATOR` e as próprias de `ADMIN`.

---

## Multi-tenancy

**Estratégia:** Schema-per-tenant no PostgreSQL.

- Schema `public` → tabela `tenants` (catálogo global)
- Schema `{schema_name}` → `users`, `roles`, `permissions`, `outbox_events`

### Resolução do tenant (chain of responsibility)

1. Header `X-Tenant-ID`
2. Claim `tenantId` no JWT
3. Subdomínio HTTP (e.g., `acme.api.company.com`)

### Contexto de tenant

```java
// O TenantContextHolder é gerenciado automaticamente pelo TenantResolutionFilter.
// Para uso manual (ex: jobs agendados):
TenantContextHolder.set("acme");
try {
    // lógica que acessa o banco do tenant "acme"
} finally {
    TenantContextHolder.clear(); // SEMPRE em finally
}
```

---

## Como Criar um Novo Tenant

1. Insira um registro na tabela `public.tenants`:

```sql
INSERT INTO public.tenants (name, schema_name, active)
VALUES ('ACME Corp', 'acme', true);
```

2. Chame `TenantSchemaInitializer` ou execute programaticamente:

```java
FlywayTenantMigrationConfig.migrateNewTenant(dataSourceProperties, "acme");
```

3. O schema `acme` é criado e as migrations `T*.sql` são aplicadas automaticamente.

> Em produção, crie um endpoint `POST /tenants` protegido por `@RequiresPermission("tenant:manage")`.

---

## Como Adicionar um Novo Módulo de Domínio (ex: `bets`)

```
Passo 1 — Core: Defina a entidade Bet e a interface BetRepository
Passo 2 — Core/Port In: Crie PlaceBetUseCase
Passo 3 — Application: Implemente PlaceBetService com @Service
Passo 4 — Infrastructure: Crie BetJpaEntity, BetJpaRepository, BetRepositoryAdapter
Passo 5 — Web: Crie BetController, BetRequest, BetResponse
Passo 6 — Migrations: Adicione T{n}__create_bets_table.sql em db/tenant-migration/
```

> As regras do ArchUnit validarão automaticamente que você não violou as dependências entre camadas.

---

## Mensageria Kafka

### Publicar um evento

```java
@Service
public class PlaceBetService {
    private final OutboxEventStore outboxEventStore;

    @Transactional
    public void placeBet(PlaceBetCommand cmd) {
        // ... lógica de negócio
        outboxEventStore.save(BetPlacedEvent.of(bet.getId(), tenantId, correlationId));
        // Evento salvo na mesma transação — publicado no Kafka pelo OutboxRelay
    }
}
```

### Consumir um evento

```java
@Component
public class BetPlacedConsumer {

    @KafkaListener(topics = "boilerplate.bet.events", groupId = "${app.kafka.consumer-group:bets}")
    public void onBetPlaced(ConsumerRecord<String, String> record) {
        // TenantContext já está populado pelo TenantAwareConsumerInterceptor
        // ...
    }
}
```

Eventos que falham após retries vão automaticamente para `{topic}.DLT` (Dead Letter Topic).

---

## Cache Redis

```java
@Service
public class MyService {
    private final CacheService cache;

    public Data getData(String id) {
        String key = CacheService.key(tenantId, "my-data", id);
        return cache.get(key, Data.class)
            .orElseGet(() -> {
                Data data = loadFromDb(id);
                cache.set(key, data, Duration.ofMinutes(5));
                return data;
            });
    }
}
```

---

## Observabilidade

| Endpoint | Descrição |
|---|---|
| `GET /actuator/health` | Status liveness e readiness |
| `GET /actuator/metrics` | Métricas Micrometer |
| `GET /actuator/prometheus` | Métricas para scraping Prometheus |

### Logs estruturados

Em produção (`spring.profiles.active=prod`), logs são emitidos como JSON compatível com ELK/Loki:

```json
{
  "@timestamp": "2024-01-01T10:00:00.000Z",
  "app": "boilerplate-api",
  "level": "INFO",
  "message": "Resolved tenant: acme",
  "requestId": "550e8400-...",
  "tenantId": "acme",
  "userId": "9f5b6c8a-..."
}
```

---

## OpenAPI / Swagger

Disponível em `http://localhost:8080/swagger-ui.html`.

- Autenticação via JWT Bearer configurada globalmente
- Endpoints públicos (`/auth/**`) marcados com `@SecurityRequirements({})`
- Tags agrupadas por bounded context

---

## Deploy

### Docker (imagem única)

```bash
docker build -f docker/Dockerfile -t boilerplate-api:latest .
docker run -p 8080:8080 --env-file .env boilerplate-api:latest
```

### Docker Compose (stack completa)

```bash
docker compose -f docker/docker-compose.yml up
```

### Kubernetes

```bash
# Criar secrets (substitua os valores)
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml   # Edite antes com valores reais
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/hpa.yaml
```

---

## Qualidade de Código

| Ferramenta | Descrição |
|---|---|
| **ArchUnit** | Valida regras de arquitetura em cada build |
| **JaCoCo** | Cobertura mínima de 80% (falha o build se abaixo) |
| **Spotless** | Formatação automática via `google-java-format` |
| **@ConfigurationProperties + @Validated** | Fail-fast se propriedades obrigatórias estiverem ausentes |

```bash
# Verificar formatação
./mvnw spotless:check

# Aplicar formatação automaticamente
./mvnw spotless:apply

# Rodar todos os testes com cobertura
./mvnw verify
```

---

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/boilerplate` | JDBC URL do PostgreSQL |
| `DB_USER` | `boilerplate` | Usuário do banco |
| `DB_PASS` | `boilerplate` | Senha do banco |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | *(vazio)* | Senha do Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do Kafka |
| `JWT_SECRET` | *(obrigatório)* | Segredo HMAC-256 (≥ 32 chars) |
| `JWT_ACCESS_TOKEN_TTL_MS` | `900000` | TTL do access token (ms) |
| `JWT_REFRESH_TOKEN_TTL_MS` | `604800000` | TTL do refresh token (ms) |

---

## Licença

MIT
