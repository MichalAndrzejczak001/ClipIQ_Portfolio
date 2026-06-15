import { useCallback, useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import type { Analysis } from '../types'
import { fetchAnalysis } from '../api/client'
import { useWebSocket } from '../hooks/useWebSocket'
import ResultCard from '../components/ResultCard'

type PageState = 'loading' | 'processing' | 'done' | 'failed'

export default function AnalysisPage() {
  const { uuid } = useParams<{ uuid: string }>()
  const [state, setState] = useState<PageState>('loading')
  const [analysis, setAnalysis] = useState<Analysis | null>(null)
  const [errorMsg, setErrorMsg] = useState('')

  const refresh = useCallback(async () => {
    if (!uuid) return
    const { data } = await fetchAnalysis(uuid)
    setAnalysis(data)
    if (data.status === 'SUCCESS') setState('done')
    else if (data.status === 'FAILED') { setState('failed'); setErrorMsg('Analiza nie powiodła się.') }
    else setState('processing')
  }, [uuid])

  useEffect(() => { refresh() }, [refresh])

  const onDone = useCallback(() => { refresh() }, [refresh])
  const onFailed = useCallback((msg: string) => { setState('failed'); setErrorMsg(msg) }, [])

  useWebSocket({ uuid: uuid ?? '', onDone, onFailed })

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-2xl">
        <Link to="/" className="text-slate-500 hover:text-brand-400 text-sm transition-colors mb-6 inline-block">
          ← Nowa analiza
        </Link>

        <div className="bg-slate-900 rounded-2xl shadow-2xl p-8">
          {state === 'loading' && (
            <p className="text-slate-400 text-center">Ładowanie…</p>
          )}

          {state === 'processing' && (
            <div className="text-center space-y-4">
              <p className="text-slate-300 text-lg font-medium">Analizuję plik…</p>
              <div className="w-full bg-slate-800 rounded-full h-2">
                <div className="bg-brand-500 h-2 rounded-full animate-pulse w-2/3" />
              </div>
              <p className="text-slate-500 text-sm">To może potrwać kilka minut</p>
            </div>
          )}

          {state === 'failed' && (
            <div className="text-center text-red-400 space-y-3">
              <p className="text-lg font-semibold">Błąd analizy</p>
              <p className="text-sm">{errorMsg}</p>
            </div>
          )}

          {state === 'done' && analysis && (
            <ResultCard analysis={analysis} />
          )}
        </div>

        {uuid && (
          <p className="text-slate-700 text-xs mt-4 text-center font-mono">{uuid}</p>
        )}
      </div>
    </div>
  )
}
