import { renderHook } from '@testing-library/react'
import { useAnalysisDataSaver } from '../hooks/useAnalysisDataSaver'
import type { Analysis } from '../types'

const saveMock = jest.fn()
const textMock = jest.fn()
const splitTextToSizeMock = jest.fn((text: string) => [text])

jest.mock('jspdf', () => ({
  __esModule: true,
  default: jest.fn().mockImplementation(() => ({
    setFillColor: jest.fn(),
    rect: jest.fn(),
    setTextColor: jest.fn(),
    setFontSize: jest.fn(),
    text: textMock,
    splitTextToSize: splitTextToSizeMock,
    addPage: jest.fn(),
    save: saveMock,
  })),
}))

const baseAnalysis: Analysis = {
  uuid: 'uuid-123',
  name: 'video.mp4',
  startDate: '2026-06-14T10:00:00',
  finishDate: '2026-06-14T10:05:00',
  status: 'SUCCESS',
  fileType: 'RAW',
  link: null,
  fullTranscription: 'Full transcription.',
  videoSummary: 'Short summary.',
  authorAttitude: 'positive',
}

describe('useAnalysisDataSaver', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('downloadPdf writes the header and saves with the analysis uuid in the filename', async () => {
    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.downloadPdf(baseAnalysis)

    expect(textMock).toHaveBeenCalledWith('ClipIQ — Wyniki analizy', 10, 13)
    expect(saveMock).toHaveBeenCalledWith('clipiq_uuid-123.pdf')
  })

  it('downloadPdf includes summary and transcription when present', async () => {
    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.downloadPdf(baseAnalysis)

    expect(textMock).toHaveBeenCalledWith('Streszczenie:', 10, expect.any(Number))
    expect(textMock).toHaveBeenCalledWith('Transkrypcja:', 10, expect.any(Number))
  })

  it('downloadPdf skips optional fields when missing', async () => {
    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.downloadPdf({
      ...baseAnalysis,
      videoSummary: null,
      fullTranscription: null,
      authorAttitude: null,
      finishDate: null,
    })

    expect(textMock).not.toHaveBeenCalledWith('Streszczenie:', 10, expect.any(Number))
    expect(textMock).not.toHaveBeenCalledWith('Nastrój autora:', 10, expect.any(Number))
    expect(textMock).not.toHaveBeenCalledWith('Data zakończenia:', 10, expect.any(Number))
  })

  it('copySummary writes the summary to the clipboard', async () => {
    const writeText = jest.fn().mockResolvedValue(undefined)
    Object.assign(navigator, { clipboard: { writeText } })

    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.copySummary(baseAnalysis)

    expect(writeText).toHaveBeenCalledWith('Short summary.')
  })

  it('copySummary does nothing when there is no summary', async () => {
    const writeText = jest.fn().mockResolvedValue(undefined)
    Object.assign(navigator, { clipboard: { writeText } })

    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.copySummary({ ...baseAnalysis, videoSummary: null })

    expect(writeText).not.toHaveBeenCalled()
  })

  it('copySummary falls back to alert when clipboard write fails', async () => {
    const writeText = jest.fn().mockRejectedValue(new Error('denied'))
    Object.assign(navigator, { clipboard: { writeText } })
    const alertMock = jest.spyOn(window, 'alert').mockImplementation(() => {})

    const { result } = renderHook(() => useAnalysisDataSaver())
    await result.current.copySummary(baseAnalysis)

    expect(alertMock).toHaveBeenCalledWith('Short summary.')
    alertMock.mockRestore()
  })
})
