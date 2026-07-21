import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthShell from '../components/AuthShell'
import { login } from '../api/auth'
import { errorMessage } from '../lib/errors'
import { validateEmail } from '../lib/validators'
import { isAdmin, useAuthStore } from '../stores/authStore'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()
  const location = useLocation()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    const emailError = validateEmail(email)
    if (emailError) {
      setError(emailError)
      return
    }
    if (!password) {
      setError('Password is required.')
      return
    }

    setSubmitting(true)
    try {
      const res = await login(email.trim(), password)
      setAuth(res.token, res.user)
      const from = (location.state as { from?: string } | null)?.from
      navigate(from ?? (isAdmin(res.user) ? '/console' : '/'), { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Log in failed. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthShell title="Log in">
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
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
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
            autoComplete="current-password"
            maxLength={72}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            placeholder="Your password"
          />
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-orange-600 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
        <p className="text-center text-sm text-gray-500">
          No account yet?{' '}
          <Link to="/register" className="font-medium text-orange-600 hover:underline">
            Register
          </Link>
        </p>
      </form>
    </AuthShell>
  )
}
