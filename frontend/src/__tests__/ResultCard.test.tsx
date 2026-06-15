import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ResultCard from '../components/ResultCard'
import type { Analysis } from '../types'

const baseAnalysis: Analysis = {
  uuid: 'test-uuid-1',
  name: 'sample-video.mp4',
  startDate: '2026-06-14T10:00:00',
  finishDate: '2026-06-14T10:05:00',
  status: 'SUCCESS',
  fileType: 'RAW',
  link: null,
  fullTranscription: 'Full transcription text here.',
  videoSummary: 'A short summary of the video content.',
  authorAttitude: 'positive',
}

describe('ResultCard', () => {
  it('renders file name', () => {
    render(<ResultCard analysis={baseAnalysis} />)
    expect(screen.getByText('sample-video.mp4')).toBeInTheDocument()
  })

  it('renders summary', () => {
    render(<ResultCard analysis={baseAnalysis} />)
    expect(screen.getByText('A short summary of the video content.')).toBeInTheDocument()
  })

  it('renders sentiment badge for positive attitude', () => {
    render(<ResultCard analysis={baseAnalysis} />)
    expect(screen.getByText('Pozytywny')).toBeInTheDocument()
  })

  it('does not render sentiment badge when attitude is null', () => {
    render(<ResultCard analysis={{ ...baseAnalysis, authorAttitude: null }} />)
    expect(screen.queryByText('Pozytywny')).not.toBeInTheDocument()
    expect(screen.queryByText('Negatywny')).not.toBeInTheDocument()
    expect(screen.queryByText('Neutralny')).not.toBeInTheDocument()
  })

  it('expands transcription on toggle click', async () => {
    render(<ResultCard analysis={baseAnalysis} />)
    expect(screen.queryByText('Full transcription text here.')).not.toBeInTheDocument()

    await userEvent.click(screen.getByText(/Transkrypcja/))
    expect(screen.getByText('Full transcription text here.')).toBeInTheDocument()
  })

  it('hides transcription section when transcription is null', () => {
    render(<ResultCard analysis={{ ...baseAnalysis, fullTranscription: null }} />)
    expect(screen.queryByText(/Transkrypcja/)).not.toBeInTheDocument()
  })
})
