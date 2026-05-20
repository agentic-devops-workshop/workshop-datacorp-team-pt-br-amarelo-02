# C4 Level 3 — Diagrama de Componentes do Backend API

```mermaid
flowchart TB
    subgraph API["Backend API (Spring Boot 3.3)"]
        direction TB

        subgraph beneficiary["Módulo: Cadastro (beneficiary)"]
            BC[BeneficiaryController]
            BS[BeneficiaryService]
            BR[BeneficiaryRepository]
        end

        subgraph eligibility["Módulo: Elegibilidade (eligibility)"]
            VS[ValidationService]
            CPF[CpfValidator]
            UF[UfValidator]
        end

        subgraph payment["Módulo: Pagamento (payment)"]
            PC[PaymentController]
            PS[PaymentService]
            CALC[BenefitCalculator]
            DSCT[DeductionCalculator]
            BATCH[BatchCycleOrchestrator]
            PR[PaymentRepository]
        end

        subgraph audit["Módulo: Auditoria (audit)"]
            AL[AuditListener]
            AR[AuditRepository]
        end

        subgraph security["Cross-cutting: Segurança"]
            SF[SecurityFilter]
            JWT[JwtDecoder]
        end
    end

    BC --> BS
    BS --> BR
    BS --> VS

    PC --> PS
    PS --> CALC
    PS --> DSCT
    PS --> BATCH
    BATCH --> PR
    BATCH --> VS

    AL --> AR

    SF --> JWT

    classDef module fill:#1e293b,stroke:#334155,color:#e2e8f0
    classDef component fill:#0f172a,stroke:#475569,color:#f1f5f9
    class beneficiary,eligibility,payment,audit,security module
    class BC,BS,BR,VS,CPF,UF,PC,PS,CALC,DSCT,BATCH,PR,AL,AR,SF,JWT component
```

## Responsabilidades por Componente

| Componente | Responsabilidade | REQ-IDs |
|-----------|-----------------|---------|
| BeneficiaryController | REST CRUD beneficiários | — |
| BeneficiaryService | Lógica de cadastro | — |
| ValidationService | Orquestra validações de entrada | REQ-ELG-001 a REQ-ELG-004 |
| CpfValidator | Módulo 11 da RF | REQ-ELG-001 |
| UfValidator | Lista 27 UFs | REQ-ELG-002 |
| PaymentController | REST para ciclo e consulta | — |
| PaymentService | Orquestração de cálculo | REQ-PAY-001, REQ-PAY-002 |
| BenefitCalculator | Fator regional + fator renda | REQ-PAY-006, REQ-PAY-007 |
| DeductionCalculator | Teto 30%, judicial, faixas | REQ-PAY-008 a REQ-PAY-011 |
| BatchCycleOrchestrator | Geração mensal, ordenação, dedup | REQ-PAY-001, REQ-PAY-003 a REQ-PAY-005 |
| AuditListener | Captura eventos de domínio | REQ-AUD-001 |
| SecurityFilter | Intercepta e valida JWT | REQ-SEC-001 |
