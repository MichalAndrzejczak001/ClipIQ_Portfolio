import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import type { Analysis } from '../types'
import { fetchAnalysis } from '../api/client'
import { useWebSocket } from '../hooks/useWebSocket'
import { useAnalysisDataSaver } from '../hooks/useAnalysisDataSaver'
import { useSystemNotificationSender } from '../hooks/useSystemNotificationSender'
import ResultCard from '../components/ResultCard'

type PageState = 'loading' | 'processing' | 'done' | 'failed'

const PROGRESS_LABELS: Record<number, string> = {
  0: 'Inicjalizacja...',
  20: 'Pobieranie pliku audio...',
  40: 'Transkrypcja (Whisper)...',
  60: 'Streszczanie (GPT)...',
  80: 'Analiza sentymentu (VADER)...',
  100: 'Zapisywanie wyników...',
}

export default function AnalysisPage() {
  const { uuid } = useParams<{ uuid: string }>()
  const [state, setState] = useState<PageState>('loading')
  const [analysis, setAnalysis] = useState<Analysis | null>(null)
  const [errorMsg, setErrorMsg] = useState('')
  const [progress, setProgress] = useState(0)

  const { downloadPdf, copySummary } = useAnalysisDataSaver()
  const { sendNotification } = useSystemNotificationSender()

  const refresh = useCallback(async () => {
    if (!uuid) return
    try {
      const { data } = await fetchAnalysis(uuid)
      setAnalysis(data)
      if (data.status === 'SUCCESS') {
        setState('done')
        sendNotification(data)
      } else if (data.status === 'FAILED') {
        setState('failed')
        setErrorMsg('Analiza nie powiodła się.')
      } else {
        setState('processing')
      }
    } catch {
      setState('failed')
      setErrorMsg('Nie udało się pobrać wyników analizy.')
    }
  }, [uuid, sendNotification])

  useEffect(() => { refresh() }, [refresh])

  const onProgress = useCallback((value: number) => setProgress(value), [])
  const onDone = useCallback(() => { refresh() }, [refresh])
  const onFailed = useCallback((msg: string) => { setState('failed'); setErrorMsg(msg) }, [])

  useWebSocket({ uuid: uuid ?? '', onProgress, onDone, onFailed })

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-2xl">
        <div className="bg-slate-900 rounded-2xl shadow-2xl p-5 sm:p-8">
          {state === 'loading' && (
            <p className="text-slate-400 text-center">Ładowanie…</p>
          )}

          {state === 'processing' && (
            <div className="text-center space-y-4">
              <p className="text-slate-300 text-lg font-medium">
                {PROGRESS_LABELS[progress] ?? 'Analizuję plik…'}
              </p>
              <div className="w-full bg-slate-800 rounded-full h-2">
                <div
                  className="bg-brand-500 h-2 rounded-full transition-all duration-500"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-slate-500 text-sm">{progress}% — to może potrwać kilka minut</p>
            </div>
          )}

          {state === 'failed' && (
            <div className="text-center text-red-400 space-y-3">
              <p className="text-lg font-semibold">Błąd analizy</p>
              <p className="text-sm">{errorMsg}</p>
            </div>
          )}

          {state === 'done' && analysis && (
            <div className="space-y-6">
              <ResultCard analysis={analysis} />
              <div className="flex gap-3 pt-4 border-t border-slate-800">
                <button
                  onClick={() => copySummary(analysis)}
                  disabled={!analysis.videoSummary}
                  className="flex-1 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 disabled:opacity-40 text-sm font-medium transition-colors"
                >
                  Kopiuj streszczenie
                </button>
                <button
                  onClick={() => downloadPdf(analysis)}
                  className="flex-1 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 text-sm font-medium transition-colors"
                >
                  Pobierz PDF
                </button>
              </div>
            </div>
          )}
        </div>

        {uuid && (
          <p className="text-slate-700 text-xs mt-4 text-center font-mono">{uuid}</p>
        )}
      </div>
    </div>
  )
}
