import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import Home from '../app/page'

describe('Home Page', () => {
  it('should render dashboard title', () => {
    render(<Home />)
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })

  it('should render navigation links for beneficiaries and payments', () => {
    render(<Home />)
    expect(screen.getByText('Beneficiários')).toBeInTheDocument()
    expect(screen.getByText('Pagamentos')).toBeInTheDocument()
    expect(screen.getByText('Auditoria')).toBeInTheDocument()
  })

  it('should have correct href for beneficiaries link', () => {
    render(<Home />)
    const link = screen.getByText('Beneficiários').closest('a')
    expect(link).toHaveAttribute('href', '/beneficiaries')
  })

  it('should have correct href for payments link', () => {
    render(<Home />)
    const link = screen.getByText('Pagamentos').closest('a')
    expect(link).toHaveAttribute('href', '/payments')
  })
})
