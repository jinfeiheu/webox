import { api } from './client'
import type { AuthUser } from '../stores/authStore'

export interface AuthResponse {
  token: string
  user: AuthUser
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>(
    '/auth/login',
    { email, password },
    { skipErrorToast: true },
  )
  return data
}

export async function register(email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>(
    '/auth/register',
    { email, password },
    { skipErrorToast: true },
  )
  return data
}

export async function fetchMe(): Promise<AuthUser> {
  const { data } = await api.get<AuthUser>('/auth/me')
  return data
}
