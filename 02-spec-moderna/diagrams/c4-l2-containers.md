# C4 Level 2 — Diagrama de Containers do SIFAP 2.0

```mermaid
C4Container
    title Diagrama de Containers — SIFAP 2.0

    Person(operador, "Operador")

    System_Boundary(sifap, "SIFAP 2.0") {
        Container(frontend, "Frontend Web", "Next.js 15, TypeScript, Tailwind, shadcn/ui", "Interface operacional para cadastro, pagamentos e consultas")
        Container(api, "Backend API", "Java 21, Spring Boot 3.3, Spring Security", "Regras de negócio, REST endpoints, orquestração de batch")
        ContainerDb(db, "Banco de Dados", "PostgreSQL 16", "Dados transacionais: beneficiários, pagamentos, auditoria")
        Container(auth, "Auth Service", "OAuth2 Resource Server", "Validação de JWT, autorização por perfil")
    }

    System_Ext(downstream, "Sistemas Downstream", "TEF/SIAFI")
    System_Ext(idp, "Identity Provider", "Azure Entra ID")

    Rel(operador, frontend, "Usa", "HTTPS/Browser")
    Rel(frontend, api, "Consome", "HTTPS/JSON REST")
    Rel(api, db, "Lê/Escreve", "JDBC/JPA")
    Rel(api, auth, "Valida token", "Internal")
    Rel(auth, idp, "Obtém chaves públicas", "OIDC/.well-known")
    Rel(api, downstream, "Exporta pagamentos", "API/Batch file")
```

## Stack por Container

| Container | Tecnologia | Justificativa |
|-----------|-----------|---------------|
| Frontend | Next.js 15 App Router + TypeScript strict + Tailwind + shadcn/ui | Stack-alvo do workshop |
| Backend API | Java 21 + Spring Boot 3.3 + JPA/Hibernate | Stack-alvo do workshop |
| Banco de Dados | PostgreSQL 16 | Substituição de Adabas (ADR-002) |
| Auth | Spring Security OAuth2 Resource Server | ADR-003 |
