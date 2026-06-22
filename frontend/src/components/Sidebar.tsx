import { NavLink } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `px-4 py-2 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
    isActive
      ? 'bg-brand-600 text-white'
      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
  }`

export default function Sidebar() {
  return (
    <nav className="w-full md:w-56 md:shrink-0 md:min-h-screen bg-slate-900 border-b md:border-b-0 md:border-r border-slate-800 px-3 py-3 md:py-6 flex flex-row md:flex-col items-center md:items-stretch gap-1">
      <span className="text-xl md:text-2xl font-extrabold text-brand-500 px-2 md:px-4 mr-2 md:mr-0 md:mb-6">
        ClipIQ
      </span>
      <NavLink to="/" end aria-label="Nowa analiza" className={linkClass}>
        Nowa analiza
      </NavLink>
      <NavLink to="/history" aria-label="Historia analiz" className={linkClass}>
        Historia
      </NavLink>
    </nav>
  )
}
