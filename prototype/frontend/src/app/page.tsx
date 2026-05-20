export default function Home() {
  const cards = [
    { href: '/beneficiaries', icon: '👥', title: 'Beneficiários', desc: 'Gerenciar cadastro de beneficiários', color: '#2563eb' },
    { href: '/payments', icon: '💰', title: 'Pagamentos', desc: 'Ciclo mensal e consultas de pagamento', color: '#059669' },
    { href: '/audit', icon: '📋', title: 'Auditoria', desc: 'Logs de eventos e rastreabilidade', color: '#7c3aed' },
  ]

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ margin: 0, fontSize: '1.5rem', color: '#1e293b' }}>Dashboard</h2>
        <p style={{ color: '#64748b', marginTop: '0.5rem' }}>Bem-vindo ao SIFAP 2.0 — Sistema modernizado</p>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem' }}>
        {cards.map((card) => (
          <a key={card.href} href={card.href} style={{
            padding: '2rem',
            background: 'white',
            border: '1px solid #e2e8f0',
            borderRadius: '0.75rem',
            textDecoration: 'none',
            color: 'inherit',
            transition: 'all 0.2s',
            boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
            borderTop: `3px solid ${card.color}`,
          }}>
            <span style={{ fontSize: '2rem' }}>{card.icon}</span>
            <h3 style={{ margin: '0.75rem 0 0.5rem', fontSize: '1.1rem', color: '#1e293b' }}>{card.title}</h3>
            <p style={{ color: '#64748b', fontSize: '0.875rem', margin: 0 }}>{card.desc}</p>
          </a>
        ))}
      </div>
    </div>
  )
}
