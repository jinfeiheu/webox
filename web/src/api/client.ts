import axios from 'axios'
import type { AxiosError } from 'axios'
import toast from 'react-hot-toast'
import { useAuthStore } from '../stores/authStore'

// Callers that render errors inline (e.g. login/register forms) pass skipErrorToast: true.
declare module 'axios' {
  interface AxiosRequestConfig {
    skipErrorToast?: boolean
  }
}

/** Matches the backend uniform error body (GlobalExceptionHandler). */
export interface ApiErrorBody {
  code: string
  message: string
  details: string[]
}

export const api = axios.create({ baseURL: '/api', timeout: 15_000 })

// Attach JWT on every request once logged in.
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Central error policy: 401 -> drop session and go to /login; others -> toast the English
// server message (ApiErrorBody.message) and rethrow so pages can react locally too.
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => {
    const status = error.response?.status
    if (status === 401) {
      useAuthStore.getState().logout()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    } else if (!error.config?.skipErrorToast) {
      const message = error.response?.data?.message ?? 'Network error. Please try again.'
      toast.error(message)
    }
    return Promise.reject(error)
  },
)
