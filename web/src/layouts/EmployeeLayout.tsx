import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { isAdmin, useAuthStore } from '../stores/authStore'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-orange-100 text-orange-700' : 'text-gray-600 hover:bg-gray-100'
  }`

/** Employee shell: top nav, horizontally scrollable on phones (PRD §1 multi-device). */
export default function EmployeeLayout() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const onLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center gap-2 px-4 py-3">
          <Link to="/" className="shrink-0 text-lg font-bold text-orange-600">
            WeBox
          </Link>
          <nav className="flex flex-1 items-center gap-1 overflow-x-auto px-2">
            <NavLink to="/" end className={navLinkClass}>
              Menu
            </NavLink>
            <NavLink to="/cart" className={navLinkClass}>
              Cart
            </NavLink>
            <NavLink to="/orders" className={navLinkClass}>
              My Orders
            </NavLink>
            <NavLink to="/settings" className={navLinkClass}>
              Settings
            </NavLink>
            {isAdmin(user) && (
              <NavLink to="/console" className={navLinkClass}>
                Console
              </NavLink>
            )}
          </nav>
          <span className="hidden shrink-0 text-sm text-gray-500 sm:block">{user?.email}</span>
          <button
            onClick={onLogout}
            className="shrink-0 rounded-md px-3 py-2 text-sm text-gray-600 hover:bg-gray-100"
          >
            Log out
          </button>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
