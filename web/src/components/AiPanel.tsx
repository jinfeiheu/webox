import { useState } from 'react'
import type { AiRecommendItem } from '../api/ai'
import { formatYuan } from '../lib/money'
import { useAuthStore } from '../stores/authStore'
import { useAddToCart } from '../hooks/useAddToCart'

/** AI chat panel (PRD §5.2): natural-language cuisine assistant with streaming results. */
export default function AiPanel() {
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)
  const [items, setItems] = useState<AiRecommendItem[]>([])
  const [error, setError] = useState<string | null>(null)
  const token = useAuthStore((s) => s.token)
  const { requestAdd, adding, dialogNode } = useAddToCart()

  const ask = () => {
    if (!query.trim() || !token) return
    setLoading(true)
    setItems([])
    setError(null)

    const es = new EventSource(
      `/api/ai/recommend?query=${encodeURIComponent(query.trim())}&token=${token}`,
    )
    es.addEventListener('item', (e) => {
      const item = JSON.parse(e.data) as AiRecommendItem
      setItems((prev) => [...prev, item])
    })
    es.addEventListener('done', () => {
      setLoading(false)
      es.close()
    })
    es.addEventListener('error', (e: MessageEvent | Event) => {
      if ('data' in e && typeof e.data === 'string') {
        setError(e.data as string)
      } else {
        setError('Failed to get recommendations. Please try again.')
      }
      setLoading(false)
      es.close()
    })
    es.onerror = () => {
      if (!loading) es.close()
    }
  }

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <h2 className="mb-3 text-sm font-semibold text-gray-900">AI Meal Assistant</h2>
      <div className="flex gap-2">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && ask()}
          maxLength={200}
          placeholder='e.g. "Something light and healthy today"'
          className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
        />
        <button
          onClick={ask}
          disabled={loading || !query.trim()}
          className="rounded-md bg-orange-600 px-4 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
        >
          {loading ? '…' : 'Ask'}
        </button>
      </div>

      {error && <p className="mt-2 text-sm text-red-500">{error}</p>}

      {items.length > 0 && (
        <div className="mt-3 space-y-2">
          {items.map((item, i) => (
            <div
              key={i}
              className="flex items-center gap-3 rounded-lg border border-gray-100 bg-gray-50 p-3"
            >
              <img
                src={item.imageUrl}
                alt={item.dishName}
                className="h-12 w-12 shrink-0 rounded object-cover"
              />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2">
                  <span className="truncate text-sm font-medium text-gray-900">
                    {item.dishName}
                  </span>
                  <span className="shrink-0 text-sm font-semibold text-orange-600">
                    {formatYuan(item.price)}
                  </span>
                </div>
                <p className="text-xs text-gray-500">{item.reason}</p>
              </div>
              <button
                onClick={() => requestAdd({ dishId: item.dishId, selectedOptions: [], qty: 1 })}
                disabled={adding}
                className="shrink-0 rounded-md bg-orange-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-orange-700 disabled:opacity-50"
              >
                Add
              </button>
            </div>
          ))}
        </div>
      )}
      {dialogNode}
    </div>
  )
}
