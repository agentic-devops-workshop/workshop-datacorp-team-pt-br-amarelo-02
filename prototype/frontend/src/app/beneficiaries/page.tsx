async function getBeneficiaries() {
  const apiUrl = process.env.API_BASE_URL || 'http://localhost:8080'
  try {
    const res = await fetch(`${apiUrl}/api/v1/beneficiaries`, { cache: 'no-store' })
    if (!res.ok) return []
    return res.json()
  } catch {
    return []
  }
}

export default async function BeneficiariesPage() {
  const beneficiaries = await getBeneficiaries()

  return (
    <div>
      <h2>Beneficiários</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #e5e7eb', textAlign: 'left' }}>
            <th style={{ padding: '0.5rem' }}>CPF</th>
            <th style={{ padding: '0.5rem' }}>Nome</th>
            <th style={{ padding: '0.5rem' }}>UF</th>
            <th style={{ padding: '0.5rem' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {beneficiaries.map((b: any) => (
            <tr key={b.cpf} style={{ borderBottom: '1px solid #f3f4f6' }}>
              <td style={{ padding: '0.5rem' }}>{b.cpf}</td>
              <td style={{ padding: '0.5rem' }}>{b.nome}</td>
              <td style={{ padding: '0.5rem' }}>{b.uf}</td>
              <td style={{ padding: '0.5rem' }}>{b.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
