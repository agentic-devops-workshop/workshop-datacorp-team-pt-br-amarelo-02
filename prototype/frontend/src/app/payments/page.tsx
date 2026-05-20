async function getPayments() {
  const res = await fetch('http://localhost:8080/api/v1/payments', { cache: 'no-store' })
  if (!res.ok) return []
  return res.json()
}

export default async function PaymentsPage() {
  const payments = await getPayments()

  return (
    <div>
      <h2>Pagamentos</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #e5e7eb', textAlign: 'left' }}>
            <th style={{ padding: '0.5rem' }}>CPF</th>
            <th style={{ padding: '0.5rem' }}>Competência</th>
            <th style={{ padding: '0.5rem' }}>Tipo</th>
            <th style={{ padding: '0.5rem' }}>Bruto</th>
            <th style={{ padding: '0.5rem' }}>Desconto</th>
            <th style={{ padding: '0.5rem' }}>Líquido</th>
            <th style={{ padding: '0.5rem' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {payments.map((p: any) => (
            <tr key={p.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
              <td style={{ padding: '0.5rem' }}>{p.cpf}</td>
              <td style={{ padding: '0.5rem' }}>{p.competencia}</td>
              <td style={{ padding: '0.5rem' }}>{p.tipoPgto}</td>
              <td style={{ padding: '0.5rem' }}>R$ {p.vlrBruto}</td>
              <td style={{ padding: '0.5rem' }}>R$ {p.vlrDesconto}</td>
              <td style={{ padding: '0.5rem' }}>R$ {p.vlrLiquido}</td>
              <td style={{ padding: '0.5rem' }}>{p.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
