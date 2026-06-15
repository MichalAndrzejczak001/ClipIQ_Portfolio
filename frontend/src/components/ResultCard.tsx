import { useState } from 'react'
import type { Analysis } from '../types'
import SentimentBadge from './SentimentBadge'

export default function ResultCard({ analysis }: { analysis: Analysis }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <h2 className="text-xl font-semibold text-slate-100 flex-1 truncate">{analysis.name}</h2>
        {analysis.authorAttitude && (
          <SentimentBadge attitude={analysis.authorAttitude} />
        )}
      </div>

      {analysis.videoSummary && (
        <section>
          <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-500 mb-2">
            Podsumowanie
          </h3>
          <p className="text-slate-200 leading-relaxed">{analysis.videoSummary}</p>
        </section>
      )}

      {analysis.fullTranscription && (
        <section>
          <button
            onClick={() => setExpanded((v) => !v)}
            className="text-xs font-semibold uppercase tracking-widest text-slate-500 hover:text-brand-400 transition-colors mb-2 flex items-center gap-1"
          >
            Transkrypcja
            <span>{expanded ? '▲' : '▼'}</span>
          </button>
          {expanded && (
            <pre className="text-slate-300 text-sm leading-relaxed whitespace-pre-wrap bg-slate-900 rounded-lg p-4 max-h-64 overflow-y-auto">
              {analysis.fullTranscription}
            </pre>
          )}
        </section>
      )}

      {analysis.finishDate && (
        <p className="text-slate-600 text-xs">
          Ukończono: {new Date(analysis.finishDate).toLocaleString('pl-PL')}
        </p>
      )}
    </div>
  )
}
