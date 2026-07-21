import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthUser {
  id: number
  email: string
  role: string // 'EMPLOYEE' | 'ADMIN' (label forms 'Employee'/'Admin' also tolerated)
}

interface AuthState {
  token: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  logout: () => void
}

// Persisted to localStorage so a page refresh keeps the login state (PRD §3.1).
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setAuth: (token, user) => set({ token, user }),
      logout: () => set({ token: null, user: null }),
    }),
    { name: 'webox-auth' },
  ),
)

export const isAdmin = (user: AuthUser | null): boolean =>
  !!user && user.role.toUpperCase() === 'ADMIN'
