export const metadata = {
  title: 'SIFAP 2.0',
  description: 'Sistema de Fiscalização e Administração de Pagamentos',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body style={{ fontFamily: 'system-ui, sans-serif', margin: 0, padding: '2rem' }}>
        <header style={{ borderBottom: '1px solid #e5e7eb', paddingBottom: '1rem', marginBottom: '2rem' }}>
          <h1 style={{ margin: 0 }}>SIFAP 2.0</h1>
          <p style={{ color: '#6b7280', margin: '0.25rem 0 0' }}>Sistema de Fiscalização e Administração de Pagamentos</p>
        </header>
        <main>{children}</main>
      </body>
    </html>
  )
}
