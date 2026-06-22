import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import HistoryPage from '../pages/HistoryPage'
import * as apiClient from '../api/client'
import type { Analysis } from '../types'

jest.mock('../api/client')
const mockedFetchAnalyses = apiClient.fetchAnalyses as jest.MockedFunction<
  typeof apiClient.fetchAnalyses
>
const mockedDeleteAnalysis = apiClient.deleteAnalysis as jest.MockedFunction<
  typeof apiClient.deleteAnalysis
>

const sampleAnalyses: Analysis[] = [
  {
    uuid: 'uuid-1',
    name: 'Video One',
    startDate: '2026-06-14T10:00:00',
    finishDate: '2026-06-14T10:05:00',
    status: 'SUCCESS',
    fileType: 'RAW',
    link: null,
    fullTranscription: 'a calm transcript',
    videoSummary: 'summary one',
    authorAttitude: 'positive',
  },
  {
    uuid: 'uuid-2',
    name: 'Video Two',
    startDate: '2026-06-14T11:00:00',
    finishDate: '2026-06-14T11:05:00',
    status: 'FAILED',
    fileType: 'YOUTUBE',
    link: 'https://youtube.com/watch?v=abc',
    fullTranscription: null,
    videoSummary: null,
    authorAttitude: null,
  },
]

function seedUuids(uuids: string[]) {
  localStorage.setItem('clipiq_analysis_uuids', JSON.stringify(uuids))
}

function renderHistoryPage() {
  return render(
    <MemoryRouter>
      <HistoryPage />
    </MemoryRouter>,
  )
}

describe('HistoryPage', () => {
  beforeEach(() => {
    localStorage.clear()
    jest.clearAllMocks()
  })

  it('shows the empty state when there is no history', () => {
    renderHistoryPage()
    expect(screen.getByText(/Brak analiz/)).toBeInTheDocument()
    expect(mockedFetchAnalyses).not.toHaveBeenCalled()
  })

  it('renders fetched analyses', async () => {
    seedUuids(['uuid-1', 'uuid-2'])
    mockedFetchAnalyses.mockResolvedValue({ data: sampleAnalyses } as never)

    renderHistoryPage()

    expect(await screen.findByText('Video One')).toBeInTheDocument()
    expect(screen.getByText('Video Two')).toBeInTheDocument()
    expect(mockedFetchAnalyses).toHaveBeenCalledWith(['uuid-1', 'uuid-2'])
  })

  it('filters the list using the search input', async () => {
    seedUuids(['uuid-1', 'uuid-2'])
    mockedFetchAnalyses.mockResolvedValue({ data: sampleAnalyses } as never)

    renderHistoryPage()
    await screen.findByText('Video One')

    await userEvent.type(
      screen.getByPlaceholderText(/Szukaj/),
      'calm transcript',
    )

    expect(screen.getByText('Video One')).toBeInTheDocument()
    expect(screen.queryByText('Video Two')).not.toBeInTheDocument()
  })

  it('shows a message when the search has no matches', async () => {
    seedUuids(['uuid-1', 'uuid-2'])
    mockedFetchAnalyses.mockResolvedValue({ data: sampleAnalyses } as never)

    renderHistoryPage()
    await screen.findByText('Video One')

    await userEvent.type(screen.getByPlaceholderText(/Szukaj/), 'nonexistent')

    expect(screen.getByText(/Brak wyników dla/)).toBeInTheDocument()
  })

  it('shows an error message when fetching fails', async () => {
    seedUuids(['uuid-1'])
    mockedFetchAnalyses.mockRejectedValue(new Error('network error'))

    renderHistoryPage()

    expect(
      await screen.findByText('Nie udało się pobrać historii analiz.'),
    ).toBeInTheDocument()
  })

  it('removes an analysis from history and from localStorage', async () => {
    seedUuids(['uuid-1', 'uuid-2'])
    mockedFetchAnalyses.mockImplementation(
      (uuids: string[]) =>
        Promise.resolve({
          data: sampleAnalyses.filter((a) => uuids.includes(a.uuid)),
        }) as never,
    )
    mockedDeleteAnalysis.mockResolvedValue({} as never)

    renderHistoryPage()
    await screen.findByText('Video One')

    await userEvent.click(screen.getAllByLabelText('Usuń z historii')[0])

    await waitFor(() => expect(screen.queryByText('Video One')).not.toBeInTheDocument())
    expect(mockedDeleteAnalysis).toHaveBeenCalledWith('uuid-1')
    expect(JSON.parse(localStorage.getItem('clipiq_analysis_uuids')!)).toEqual(['uuid-2'])
  })

  it('keeps the analysis in history when deleting fails on the server', async () => {
    seedUuids(['uuid-1', 'uuid-2'])
    mockedFetchAnalyses.mockResolvedValue({ data: sampleAnalyses } as never)
    mockedDeleteAnalysis.mockRejectedValue(new Error('network error'))

    renderHistoryPage()
    await screen.findByText('Video One')

    await userEvent.click(screen.getAllByLabelText('Usuń z historii')[0])

    expect(await screen.findByText('Nie udało się usunąć analizy.')).toBeInTheDocument()
    expect(screen.getByText('Video One')).toBeInTheDocument()
    expect(JSON.parse(localStorage.getItem('clipiq_analysis_uuids')!)).toEqual(['uuid-1', 'uuid-2'])
  })
})
