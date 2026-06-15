import { useNavigate } from 'react-router-dom'
import UploadForm from '../components/UploadForm'

export default function HomePage() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4">
      <header className="text-center mb-10">
        <h1 className="text-5xl font-extrabold tracking-tight text-brand-500 mb-2">ClipIQ</h1>
        <p className="text-slate-400 text-lg">Transkrypcja i analiza wideo z AI</p>
      </header>

      <main className="w-full max-w-lg bg-slate-900 rounded-2xl shadow-2xl p-8">
        <UploadForm onSubmit={(uuid) => navigate(`/analysis/${uuid}`)} />
      </main>
    </div>
  )
}
