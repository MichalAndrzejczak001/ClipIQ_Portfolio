import type { AuthorAttitude } from '../types'

const config: Record<AuthorAttitude, { label: string; classes: string }> = {
  positive: { label: 'Pozytywny', classes: 'bg-green-900 text-green-300 border-green-700' },
  negative: { label: 'Negatywny', classes: 'bg-red-900 text-red-300 border-red-700' },
  neutral:  { label: 'Neutralny', classes: 'bg-slate-700 text-slate-300 border-slate-500' },
}

export default function SentimentBadge({ attitude }: { attitude: AuthorAttitude }) {
  const { label, classes } = config[attitude]
  return (
    <span className={`inline-block px-3 py-1 rounded-full border text-sm font-medium ${classes}`}>
      {label}
    </span>
  )
}
