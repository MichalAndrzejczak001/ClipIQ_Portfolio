import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import UploadForm from '../components/UploadForm'
import * as apiClient from '../api/client'

jest.mock('../api/client')
const mockedRegisterUrl = apiClient.registerUrl as jest.MockedFunction<typeof apiClient.registerUrl>
const mockedRegisterFile = apiClient.registerFile as jest.MockedFunction<typeof apiClient.registerFile>

describe('UploadForm', () => {
  const user = userEvent.setup({ delay: null })

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('renders file mode by default', () => {
    render(<UploadForm onSubmit={jest.fn()} />)
    expect(screen.getByText('Plik (MP3/MP4)')).toBeInTheDocument()
    expect(screen.getByText(/Przeciągnij plik lub kliknij/)).toBeInTheDocument()
  })

  it('switches to URL mode', async () => {
    render(<UploadForm onSubmit={jest.fn()} />)
    await user.click(screen.getByText('Link (YouTube/TikTok)'))
    expect(screen.getByPlaceholderText(/youtube/i)).toBeInTheDocument()
  })

  it('calls registerUrl and invokes onSubmit with uuid for valid URL', async () => {
    const onSubmit = jest.fn()
    mockedRegisterUrl.mockResolvedValue({ data: { analysisUuid: 'uuid-123' } } as never)

    render(<UploadForm onSubmit={onSubmit} />)
    await user.click(screen.getByText('Link (YouTube/TikTok)'))
    await user.type(screen.getByPlaceholderText(/youtube/i), 'https://www.youtube.com/watch?v=abc')
    await user.click(screen.getByRole('button', { name: /Analizuj/ }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith('uuid-123'))
  })

  it('shows error message when server rejects the request', async () => {
    mockedRegisterUrl.mockRejectedValue(new Error('Server error'))

    render(<UploadForm onSubmit={jest.fn()} />)
    await user.click(screen.getByText('Link (YouTube/TikTok)'))
    await user.type(screen.getByPlaceholderText(/youtube/i), 'https://www.youtube.com/watch?v=abc')
    await user.click(screen.getByRole('button', { name: /Analizuj/ }))

    await waitFor(() => expect(screen.getByText(/Błąd serwera/)).toBeInTheDocument())
  })

  it('shows a specific message for unsupported file format (HTTP 400)', async () => {
    mockedRegisterUrl.mockRejectedValue({
      isAxiosError: true,
      response: { status: 400 },
    })

    render(<UploadForm onSubmit={jest.fn()} />)
    await user.click(screen.getByText('Link (YouTube/TikTok)'))
    await user.type(screen.getByPlaceholderText(/youtube/i), 'https://www.youtube.com/watch?v=abc')
    await user.click(screen.getByRole('button', { name: /Analizuj/ }))

    await waitFor(() =>
      expect(screen.getByText(/Nieprawidłowy adres URL/)).toBeInTheDocument(),
    )
  })

  it('shows a specific message when the file is too large (HTTP 413)', async () => {
    mockedRegisterFile.mockRejectedValue({
      isAxiosError: true,
      response: { status: 413 },
    })

    const { container } = render(<UploadForm onSubmit={jest.fn()} />)
    const fileInput = container.querySelector('input[type="file"]') as HTMLInputElement
    const file = new File(['x'], 'huge-video.mp4', { type: 'video/mp4' })
    await user.upload(fileInput, file)
    await user.click(screen.getByRole('button', { name: /Analizuj/ }))

    await waitFor(() =>
      expect(screen.getByText(/Plik jest za duży/)).toBeInTheDocument(),
    )
  })

  it('shows error when submitting file mode without selecting a file', async () => {
    render(<UploadForm onSubmit={jest.fn()} />)
    await user.click(screen.getByRole('button', { name: /Analizuj/ }))
    expect(screen.getByText(/Wybierz plik/)).toBeInTheDocument()
    expect(mockedRegisterFile).not.toHaveBeenCalled()
  })
})
