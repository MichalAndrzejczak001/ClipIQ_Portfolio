import { useNavigate } from 'react-router-dom'
import UploadForm from '../components/UploadForm'
import { useAnalysisIdsRepository } from '../hooks/useAnalysisIdsRepository'

export default function HomePage() {
  const navigate = useNavigate()
  const { addUuid } = useAnalysisIdsRepository()

  const handleSubmit = (uuid: string) => {
    addUuid(uuid)
    navigate(`/analysis/${uuid}`)
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4">
      <header className="text-center mb-10">
        <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-brand-500 mb-2">ClipIQ</h1>
        <p className="text-slate-400 text-lg">Transkrypcja i analiza wideo z AI</p>
      </header>

      <main className="w-full max-w-lg bg-slate-900 rounded-2xl shadow-2xl p-5 sm:p-8">
        <UploadForm onSubmit={handleSubmit} />
      </main>
    </div>
  )
}
