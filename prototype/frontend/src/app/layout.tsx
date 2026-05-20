export const metadata = {
  title: 'SIFAP 2.0',
  description: 'Sistema de Fiscalização e Administração de Pagamentos',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body style={{ fontFamily: "'Segoe UI', system-ui, -apple-system, sans-serif", margin: 0, padding: 0, minHeight: '100vh', position: 'relative', background: '#f8fafc' }}>
        {/* Watermark background */}
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundImage: 'url(/background.jpeg)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          opacity: 0.4,
          zIndex: 0,
          pointerEvents: 'none',
        }} />

        {/* Header */}
        <header style={{
          position: 'relative',
          zIndex: 10,
          background: 'linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)',
          color: 'white',
          padding: '1.25rem 2rem',
          boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, letterSpacing: '-0.02em' }}>
              SIFAP 2.0
            </h1>
            <p style={{ color: 'rgba(255,255,255,0.8)', margin: '0.25rem 0 0', fontSize: '0.85rem' }}>
              Sistema de Fiscalização e Administração de Pagamentos
            </p>
          </div>
          <nav style={{ display: 'flex', gap: '1.5rem' }}>
            <a href="/" style={{ color: 'rgba(255,255,255,0.9)', textDecoration: 'none', fontSize: '0.9rem', fontWeight: 500 }}>Dashboard</a>
            <a href="/beneficiaries" style={{ color: 'rgba(255,255,255,0.9)', textDecoration: 'none', fontSize: '0.9rem', fontWeight: 500 }}>Beneficiários</a>
            <a href="/payments" style={{ color: 'rgba(255,255,255,0.9)', textDecoration: 'none', fontSize: '0.9rem', fontWeight: 500 }}>Pagamentos</a>
            <a href="/audit" style={{ color: 'rgba(255,255,255,0.9)', textDecoration: 'none', fontSize: '0.9rem', fontWeight: 500 }}>Auditoria</a>
          </nav>
        </header>

        {/* Main content */}
        <main style={{
          position: 'relative',
          zIndex: 5,
          maxWidth: '1200px',
          margin: '2rem auto',
          padding: '0 2rem',
        }}>
          <div style={{
            background: 'rgba(255,255,255,0.92)',
            backdropFilter: 'blur(8px)',
            borderRadius: '1rem',
            padding: '2rem',
            boxShadow: '0 4px 24px rgba(0,0,0,0.06)',
            border: '1px solid rgba(0,0,0,0.06)',
          }}>
            {children}
          </div>
        </main>

        {/* Footer */}
        <footer style={{
          position: 'relative',
          zIndex: 10,
          textAlign: 'center',
          padding: '1.5rem',
          color: '#6b7280',
          fontSize: '0.8rem',
        }}>
          SIFAP 2.0 — Modernização de Legado Natural/Adabas • Workshop 2026
        </footer>
      </body>
    </html>
  )
}
