export default function Home() {
  return (
    <div>
      <h2>Dashboard</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginTop: '1rem' }}>
        <a href="/beneficiaries" style={{ padding: '1.5rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none', color: 'inherit' }}>
          <h3>Beneficiários</h3>
          <p style={{ color: '#6b7280' }}>Gerenciar cadastro</p>
        </a>
        <a href="/payments" style={{ padding: '1.5rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none', color: 'inherit' }}>
          <h3>Pagamentos</h3>
          <p style={{ color: '#6b7280' }}>Ciclo mensal e consultas</p>
        </a>
        <a href="/audit" style={{ padding: '1.5rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none', color: 'inherit' }}>
          <h3>Auditoria</h3>
          <p style={{ color: '#6b7280' }}>Log de eventos</p>
        </a>
      </div>
    </div>
  )
}
