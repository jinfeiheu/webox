import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthShell from '../components/AuthShell'
import { register } from '../api/auth'
import { errorMessage } from '../lib/errors'
import { PASSWORD_HINT, validateEmail, validatePassword } from '../lib/validators'
import { useAuthStore } from '../stores/authStore'

export default function RegisterPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    const firstError =
      validateEmail(email) ??
      validatePassword(password) ??
      (password !== confirm ? 'Passwords do not match.' : null)
    if (firstError) {
      setError(firstError)
      return
    }

    setSubmitting(true)
    try {
      const res = await register(email.trim(), password)
      setAuth(res.token, res.user)
      navigate('/', { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Registration failed. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  const inputClass =
    'w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none'

  return (
    <AuthShell title="Register">
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        {error && (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}
        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium text-gray-700">
            Work email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            maxLength={200}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={inputClass}
            placeholder="you@company.com"
          />
        </div>
        <div>
          <label htmlFor="password" className="mb-1 block text-sm font-medium text-gray-700">
            Password
          </label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            maxLength={72}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={inputClass}
            placeholder={PASSWORD_HINT}
          />
          <p className="mt-1 text-xs text-gray-400">{PASSWORD_HINT}</p>
        </div>
        <div>
          <label htmlFor="confirm" className="mb-1 block text-sm font-medium text-gray-700">
            Confirm password
          </label>
          <input
            id="confirm"
            type="password"
            autoComplete="new-password"
            maxLength={72}
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            className={inputClass}
            placeholder="Repeat your password"
          />
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-orange-600 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
        <p className="text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-orange-600 hover:underline">
            Log in
          </Link>
        </p>
      </form>
    </AuthShell>
  )
}
