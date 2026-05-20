# C4 Level 1 — Diagrama de Contexto do SIFAP 2.0

```mermaid
C4Context
    title Diagrama de Contexto — SIFAP 2.0

    Person(operador, "Operador", "Servidor público que registra beneficiários e opera pagamentos")
    Person(auditor, "Auditor", "Profissional que consulta trilha de auditoria e relatórios")
    Person(admin, "Administrador", "Gerencia usuários, perfis e configurações do sistema")

    System(sifap, "SIFAP 2.0", "Sistema de Fiscalização e Acompanhamento de Pagamentos — modernizado")

    System_Ext(downstream, "Sistemas Financeiros Downstream", "TEF/SIAFI — processa pagamentos aprovados")
    System_Ext(auth_provider, "Identity Provider", "Azure Entra ID — OAuth2/OIDC")

    Rel(operador, sifap, "Opera cadastros e ciclos de pagamento", "HTTPS")
    Rel(auditor, sifap, "Consulta auditoria e relatórios", "HTTPS")
    Rel(admin, sifap, "Administra acessos e parâmetros", "HTTPS")
    Rel(sifap, downstream, "Exporta pagamentos processados", "API/Batch")
    Rel(sifap, auth_provider, "Valida tokens JWT", "OIDC")
```

## Notas

- O SIFAP 2.0 substitui o sistema legado Natural/Adabas (1997–2025).
- Sistemas downstream dependem da ordenação por CPF (BR-013) — contrato a ser formalizado.
- Identity Provider pode ser Azure Entra ID ou Keycloak local para desenvolvimento.
