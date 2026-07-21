/** Shared form validation rules (PRD §3.1). Messages are English per PRD §1. */

export const EMAIL_MAX = 200
export const SEARCH_MAX = 50
export const ADDRESS_MAX = 200

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** >= 8 chars, must contain both letters and digits (mirrors the backend rule). */
const PASSWORD_RE = /^(?=.*[A-Za-z])(?=.*\d)\S{8,72}$/

export const PASSWORD_HINT = 'At least 8 characters, containing both letters and numbers.'

/** Returns an error message, or null when valid. */
export function validateEmail(email: string): string | null {
  if (!email.trim()) return 'Email is required.'
  if (email.length > EMAIL_MAX) return 'Email must be at most 200 characters.'
  if (!EMAIL_RE.test(email)) return 'Please enter a valid email address.'
  return null
}

export function validatePassword(password: string): string | null {
  if (!PASSWORD_RE.test(password)) return PASSWORD_HINT
  return null
}
