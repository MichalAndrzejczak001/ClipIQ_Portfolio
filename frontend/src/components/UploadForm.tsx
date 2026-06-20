import { useState, useRef } from 'react'
import axios from 'axios'
import { registerFile, registerUrl } from '../api/client'

interface Props {
  onSubmit: (uuid: string) => void
}

export default function UploadForm({ onSubmit }: Props) {
  const [mode, setMode] = useState<'file' | 'url'>('file')
  const [url, setUrl] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      if (mode === 'url') {
        const { data } = await registerUrl(url)
        onSubmit(data.analysisUuid)
      } else {
        if (!file) { setError('Wybierz plik MP3 lub MP4'); setLoading(false); return }
        const { data } = await registerFile(file)
        onSubmit(data.analysisUuid)
      }
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 400) {
        setError(
          mode === 'file'
            ? 'Niewspierany format pliku lub uszkodzony plik. Użyj MP3 lub MP4.'
            : 'Nieprawidłowy adres URL. Wklej link do YouTube lub TikTok.',
        )
      } else {
        setError('Błąd serwera. Spróbuj ponownie później.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* mode switcher */}
      <div className="flex rounded-lg overflow-hidden border border-slate-700">
        {(['file', 'url'] as const).map((m) => (
          <button
            key={m}
            type="button"
            onClick={() => { setMode(m); setError(null) }}
            className={`flex-1 py-2 text-sm font-medium transition-colors ${
              mode === m
                ? 'bg-brand-600 text-white'
                : 'bg-slate-800 text-slate-400 hover:text-slate-200'
            }`}
          >
            {m === 'file' ? 'Plik (MP3/MP4)' : 'Link (YouTube/TikTok)'}
          </button>
        ))}
      </div>

      {mode === 'file' ? (
        <div
          className="border-2 border-dashed border-slate-600 rounded-xl p-10 text-center cursor-pointer hover:border-brand-500 transition-colors"
          onClick={() => inputRef.current?.click()}
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault()
            const f = e.dataTransfer.files[0]
            if (f) setFile(f)
          }}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".mp3,.mp4"
            className="hidden"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
          {file ? (
            <p className="text-brand-400 font-medium">{file.name}</p>
          ) : (
            <>
              <p className="text-slate-300 mb-1">Przeciągnij plik lub kliknij</p>
              <p className="text-slate-500 text-sm">MP3 lub MP4</p>
            </>
          )}
        </div>
      ) : (
        <input
          type="url"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://www.youtube.com/watch?v=..."
          required
          className="w-full rounded-lg bg-slate-800 border border-slate-600 px-4 py-3 text-slate-100 placeholder-slate-500 focus:outline-none focus:border-brand-500"
        />
      )}

      {error && (
        <p className="text-red-400 text-sm text-center">{error}</p>
      )}

      <button
        type="submit"
        disabled={loading}
        className="w-full py-3 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:opacity-50 font-semibold transition-colors"
      >
        {loading ? 'Wysyłanie…' : 'Analizuj'}
      </button>
    </form>
  )
}
