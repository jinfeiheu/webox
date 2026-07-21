import type { ReactElement } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { isAdmin, useAuthStore } from '../stores/authStore'

interface Props {
  children: ReactElement
  role?: 'ADMIN'
}

/** Client-side route guard. Backend still enforces authorization per request. */
export default function RequireAuth({ children, role }: Props) {
  const { token, user } = useAuthStore()
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (role === 'ADMIN' && !isAdmin(user)) {
    return <Navigate to="/" replace />
  }
  return children
}
