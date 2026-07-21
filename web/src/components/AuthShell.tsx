import type { ReactNode } from 'react'

/** Centered card shell for the public auth pages (no employee nav). */
export default function AuthShell({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="mb-6 text-center">
          <div className="text-3xl font-bold text-orange-600">WeBox</div>
          <p className="mt-1 text-sm text-gray-500">Employee Meal Ordering Platform</p>
        </div>
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <h1 className="mb-4 text-xl font-semibold text-gray-900">{title}</h1>
          {children}
        </div>
      </div>
    </div>
  )
}
