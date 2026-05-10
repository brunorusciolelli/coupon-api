# Coupon API

API REST para gerenciamento de cupons desenvolvida com Spring Boot 3 e Java 17. Implementa o desafio técnico proposto pela [Outforce](https://outforce.com.br/).

## Tecnologias

- **Java 17** (Eclipse Temurin)
- **Spring Boot 3.5** — Web, Data JPA, Validation, DevTools
- **H2** banco de dados em memória
- **Maven** ferramenta de build
- **JUnit 5 + Mockito + AssertJ** testes
- **springdoc-openapi** para Swagger UI
- **Docker + Docker Compose**

## Arquitetura

O projeto segue uma arquitetura em camadas com separação clara entre **regras de negócio (domínio)** e **persistência (entidade JPA)**:

```
com.brunorusciolelli.coupon
├── domain/entity      # Domínio puro — regras de negócio sem dependência de framework
├── controller         # Camada HTTP
├── service            # Casos de uso e orquestração
├── repository         # Entidade JPA, repositório Spring Data e mapper
├── dto                # Payloads de requisição e resposta
├── exception          # Exceções customizadas e handler global
└── config             # Configuração do OpenAPI
```

A classe de domínio `Coupon` encapsula todas as validações e mutações de estado. Nenhuma regra de negócio vaza para o serviço ou controller.

## Regras de Negócio

### Criar
- `code`, `description`, `discountValue` e `expirationDate` são obrigatórios
- `code` deve ter exatamente 6 caracteres alfanuméricos; caracteres especiais são aceitos na entrada mas removidos antes de salvar
- `discountValue` mínimo é `0.5`, sem limite máximo
- `expirationDate` não pode estar no passado
- Um cupom pode ser criado já publicado

### Deletar
- Apenas soft delete (o status muda para `DELETED`, nenhuma linha é removida do banco)
- Não é possível deletar um cupom já deletado

## Como Executar

### Pré-requisitos

- Java 17
- Maven 3.9+ (ou use o Maven Wrapper incluído no projeto)
- Docker + Docker Compose (apenas se for rodar com Docker)

### Rodar localmente

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### Rodar com Docker

```bash
docker compose up --build
```

Para parar:

```bash
docker compose down
```

## Documentação da API

Com a aplicação rodando, o Swagger UI estará disponível em:

**http://localhost:8080/swagger-ui/index.html**

### Endpoints

| Método | Rota             | Descrição                  | Status              |
|--------|------------------|----------------------------|---------------------|
| POST   | `/coupon`        | Criar um novo cupom        | 201, 400            |
| GET    | `/coupon/{id}`   | Buscar cupom por ID        | 200, 404            |
| DELETE | `/coupon/{id}`   | Soft delete de um cupom    | 204, 400, 404       |

### Exemplo de requisição

```bash
curl -X POST http://localhost:8080/coupon \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ABC-123",
    "description": "Desconto Black Friday",
    "discountValue": 0.8,
    "expirationDate": "2027-01-01T00:00:00Z",
    "published": true
  }'
```

### Exemplo de resposta

```json
{
  "id": "cef9d1e3-aae5-4ab6-a297-358c6032b1e7",
  "code": "ABC123",
  "description": "Desconto Black Friday",
  "discountValue": 0.8,
  "expirationDate": "2027-01-01T00:00:00Z",
  "status": "ACTIVE",
  "published": true,
  "redeemed": false
}
```

## Testes

```bash
./mvnw test
```

A suíte cobre:
- **Testes unitários** do domínio `Coupon` (regras de negócio)
- **Testes unitários** do `CouponMapper` (mapeamento de ida e volta)
- **Testes unitários** do `CouponService` (com Mockito)
- **Testes de integração** do `CouponController` (contexto Spring completo com H2)

## Autor

Bruno Rusciolelli — [GitHub](https://github.com/brunorusciolelli)
