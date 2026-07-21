import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

const sideLinkClass = ({ isActive }: { isActive: boolean }) =>
  `block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-gray-900 text-white' : 'text-gray-600 hover:bg-gray-100'
  }`

/** Console shell (PRD §4.3): separate sidebar navigation, distinct from the employee UI. */
export default function ConsoleLayout() {
  const { user, logout } = useAuthStore()

  return (
    <div className="flex min-h-screen">
      <aside className="w-52 shrink-0 border-r border-gray-200 bg-white p-4">
        <div className="mb-1 text-lg font-bold text-gray-900">WeBox Console</div>
        <div className="mb-4 truncate text-xs text-gray-400">{user?.email}</div>
        <nav className="space-y-1">
          <NavLink to="/console/dishes" className={sideLinkClass}>
            Dishes
          </NavLink>
          <NavLink to="/console/menus" className={sideLinkClass}>
            Daily Menus
          </NavLink>
          <NavLink to="/console/dashboard" className={sideLinkClass}>
            Dashboard
          </NavLink>
        </nav>
        <div className="mt-6 space-y-1 border-t border-gray-100 pt-4">
          <Link to="/" className="block rounded-md px-3 py-2 text-sm text-gray-600 hover:bg-gray-100">
            Employee view
          </Link>
          <button
            onClick={logout}
            className="block w-full rounded-md px-3 py-2 text-left text-sm text-gray-600 hover:bg-gray-100"
          >
            Log out
          </button>
        </div>
      </aside>
      <main className="min-w-0 flex-1 p-6">
        <Outlet />
      </main>
    </div>
  )
}
