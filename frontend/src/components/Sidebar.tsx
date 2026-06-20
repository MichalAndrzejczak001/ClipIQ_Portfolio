import { NavLink } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `block px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
    isActive
      ? 'bg-brand-600 text-white'
      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
  }`

export default function Sidebar() {
  return (
    <nav className="w-56 shrink-0 min-h-screen bg-slate-900 border-r border-slate-800 px-3 py-6 flex flex-col gap-1">
      <span className="text-2xl font-extrabold text-brand-500 px-4 mb-6 block">ClipIQ</span>
      <NavLink to="/" end aria-label="Nowa analiza" className={linkClass}>
        Nowa analiza
      </NavLink>
      <NavLink to="/history" aria-label="Historia analiz" className={linkClass}>
        Historia
      </NavLink>
    </nav>
  )
}
