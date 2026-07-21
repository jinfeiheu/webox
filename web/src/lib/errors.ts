import axios from 'axios'
import type { ApiErrorBody } from '../api/client'

/** Extracts the backend's English error message (plus first detail) for inline display. */
export function errorMessage(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err)) {
    const body = err.response?.data as ApiErrorBody | undefined
    if (body?.message) {
      const detail = body.details?.[0]
      return detail ? `${body.message} ${detail}` : body.message
    }
  }
  return fallback
}
