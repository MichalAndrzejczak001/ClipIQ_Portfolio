import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Sidebar from '../components/Sidebar'

describe('Sidebar', () => {
  it('renders links to home and history', () => {
    render(
      <MemoryRouter>
        <Sidebar />
      </MemoryRouter>,
    )
    expect(screen.getByRole('link', { name: 'Nowa analiza' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('link', { name: 'Historia analiz' })).toHaveAttribute(
      'href',
      '/history',
    )
  })

  it('marks the home link as active on "/"', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Sidebar />
      </MemoryRouter>,
    )
    expect(screen.getByRole('link', { name: 'Nowa analiza' })).toHaveClass('bg-brand-600')
    expect(screen.getByRole('link', { name: 'Historia analiz' })).not.toHaveClass('bg-brand-600')
  })

  it('marks the history link as active on "/history"', () => {
    render(
      <MemoryRouter initialEntries={['/history']}>
        <Sidebar />
      </MemoryRouter>,
    )
    expect(screen.getByRole('link', { name: 'Historia analiz' })).toHaveClass('bg-brand-600')
    expect(screen.getByRole('link', { name: 'Nowa analiza' })).not.toHaveClass('bg-brand-600')
  })
})
