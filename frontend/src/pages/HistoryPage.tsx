import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import type { Analysis } from '../types'
import { deleteAnalysis, fetchAnalyses } from '../api/client'
import { useAnalysisIdsRepository } from '../hooks/useAnalysisIdsRepository'
import SentimentBadge from '../components/SentimentBadge'

const STATUS_LABELS: Record<Analysis['status'], string> = {
  IN_PROGRESS: 'W trakcie',
  SUCCESS: 'Zakończona',
  FAILED: 'Niepowodzenie',
}

const STATUS_CLASSES: Record<Analysis['status'], string> = {
  IN_PROGRESS: 'text-yellow-400',
  SUCCESS: 'text-green-400',
  FAILED: 'text-red-400',
}

export default function HistoryPage() {
  const { uuids, removeUuid } = useAnalysisIdsRepository()
  const [analyses, setAnalyses] = useState<Analysis[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  useEffect(() => {
    if (uuids.length === 0) {
      setAnalyses([])
      return
    }
    let cancelled = false
    setLoading(true)
    setFetchError(null)
    fetchAnalyses(uuids)
      .then(({ data }) => {
        if (!cancelled) setAnalyses(data)
      })
      .catch(() => {
        if (!cancelled) setFetchError('Nie udało się pobrać historii analiz.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [uuids])

  useEffect(() => {
    const hasInProgress = analyses.some((a) => a.status === 'IN_PROGRESS')
    if (!hasInProgress || uuids.length === 0) return
    const interval = setInterval(() => {
      fetchAnalyses(uuids)
        .then(({ data }) => setAnalyses(data))
        .catch(() => {})
    }, 3000)
    return () => clearInterval(interval)
  }, [analyses, uuids])

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) return analyses
    return analyses.filter((analysis) =>
      [
        analysis.name,
        analysis.status,
        analysis.fileType,
        analysis.authorAttitude,
        analysis.videoSummary,
        analysis.fullTranscription,
        analysis.startDate,
        analysis.finishDate,
      ]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(query)),
    )
  }, [analyses, search])

  const handleRemove = async (uuid: string) => {
    setDeleteError(null)
    try {
      await deleteAnalysis(uuid)
      removeUuid(uuid)
    } catch {
      setDeleteError('Nie udało się usunąć analizy.')
    }
  }

  return (
    <div className="min-h-screen px-4 py-10 max-w-3xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-slate-100">Historia analiz</h2>
      </div>

      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Szukaj po nazwie, statusie, treści transkrypcji..."
        className="w-full rounded-lg bg-slate-800 border border-slate-600 px-4 py-2 text-slate-100 placeholder-slate-500 mb-6 focus:outline-none focus:border-brand-500"
      />

      {loading && <p className="text-slate-400">Ładowanie...</p>}
      {fetchError && <p className="text-red-400">{fetchError}</p>}
      {deleteError && <p className="text-red-400">{deleteError}</p>}
      {!loading && !fetchError && uuids.length === 0 && (
        <p className="text-slate-500">Brak analiz. Rozpocznij nową analizę na stronie głównej.</p>
      )}
      {!loading && !fetchError && uuids.length > 0 && filtered.length === 0 && (
        <p className="text-slate-500">Brak wyników dla: „{search}”</p>
      )}

      <ul className="space-y-3">
        {filtered.map((analysis) => (
          <li
            key={analysis.uuid}
            className="bg-slate-800 border border-slate-700 rounded-lg p-4 hover:border-brand-500 transition-colors flex items-center gap-4"
          >
            <Link to={`/analysis/${analysis.uuid}`} className="flex-1 min-w-0">
              <p className="text-slate-100 font-medium truncate">{analysis.name}</p>
              <p className="text-xs text-slate-500">
                {new Date(analysis.startDate).toLocaleString('pl-PL')}
              </p>
            </Link>
            {analysis.authorAttitude && <SentimentBadge attitude={analysis.authorAttitude} />}
            <span className={`text-sm font-medium ${STATUS_CLASSES[analysis.status]}`}>
              {STATUS_LABELS[analysis.status]}
            </span>
            <button
              onClick={() => handleRemove(analysis.uuid)}
              className="text-slate-500 hover:text-red-400 transition-colors"
              aria-label="Usuń z historii"
            >
              ✕
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}
