import { render, screen } from '@testing-library/react'
import SentimentBadge from '../components/SentimentBadge'

describe('SentimentBadge', () => {
  it('renders positive sentiment', () => {
    render(<SentimentBadge attitude="positive" />)
    expect(screen.getByText('Pozytywny')).toBeInTheDocument()
  })

  it('renders negative sentiment', () => {
    render(<SentimentBadge attitude="negative" />)
    expect(screen.getByText('Negatywny')).toBeInTheDocument()
  })

  it('renders neutral sentiment', () => {
    render(<SentimentBadge attitude="neutral" />)
    expect(screen.getByText('Neutralny')).toBeInTheDocument()
  })
})
