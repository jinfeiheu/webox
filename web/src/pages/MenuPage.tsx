import { useEffect, useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchMenu, searchMenu } from '../api/menu'
import { fetchPreferences } from '../api/preferences'
import DishCard from '../components/DishCard'
import { CATEGORIES } from '../lib/constants'
import { SEARCH_MAX } from '../lib/validators'
import { useAuthStore } from '../stores/authStore'

const PAGE_SIZE = 12

/** Home page (PRD §3.2/§4.1): today's menu with multi-category filter, debounced search,
 *  pagination, and a "For You" switch that boosts preferred cuisines + highlights spice matches. */
export default function MenuPage() {
  const [keyword, setKeyword] = useState('')
  const [debouncedKeyword, setDebouncedKeyword] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)
  const [forYou, setForYou] = useState(false)
  const queryClient = useQueryClient()
  const token = useAuthStore((s) => s.token)

  // Real-time inventory: stock changes pushed by the server update the menu cache (PRD §5.1).
  useEffect(() => {
    if (!token) return
    const es = new EventSource(`/api/inventory/stream?token=${token}`)
    const onStock = () => {
      queryClient.invalidateQueries({ queryKey: ['menu'] })
      queryClient.invalidateQueries({ queryKey: ['cart'] })
    }
    es.addEventListener('stock', onStock)
    es.onerror = () => {}  // reconnect is automatic
    return () => es.close()
  }, [token, queryClient])

  // 300ms debounce — the 9:30-10:00 peak must not hammer the search API per keystroke.
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedKeyword(keyword.trim()), 300)
    return () => clearTimeout(timer)
  }, [keyword])

  const filtering = debouncedKeyword.length > 0 || selectedCategories.length > 0
  const queryKey = ['menu', 'today', debouncedKeyword, selectedCategories]

  const { data, isLoading, isError } = useQuery({
    queryKey,
    queryFn: () =>
      filtering
        ? searchMenu({ q: debouncedKeyword, categories: selectedCategories })
        : fetchMenu(),
  })

  // Preferences only load once the employee opts into "For You" (cached afterwards).
  const { data: prefs } = useQuery({
    queryKey: ['preferences'],
    queryFn: fetchPreferences,
    enabled: forYou,
  })

  // Reset pagination whenever the result set changes.
  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [queryKey.join('|')])

  const rawItems = useMemo(() => data?.items ?? [], [data])

  // "For You": stable-sort preferred cuisines to the front; mark spice matches for highlight.
  const { items, highlightSet } = useMemo(() => {
    if (!forYou || !prefs) {
      return { items: rawItems, highlightSet: new Set<number>() }
    }
    const preferred = new Set(prefs.cuisines)
    const sorted = [...rawItems].sort((a, b) => {
      const pa = preferred.has(a.category) ? 0 : 1
      const pb = preferred.has(b.category) ? 0 : 1
      return pa - pb
    })
    const highlights = new Set<number>(
      prefs.spiceLevel
        ? sorted.filter((i) => i.spiceLevel === prefs.spiceLevel).map((i) => i.dishId)
        : [],
    )
    return { items: sorted, highlightSet: highlights }
  }, [rawItems, forYou, prefs])

  const visibleItems = items.slice(0, visibleCount)

  const toggleCategory = (category: string) => {
    setSelectedCategories((prev) =>
      prev.includes(category) ? prev.filter((c) => c !== category) : [...prev, category],
    )
  }

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">Today's Menu</h1>
        <div className="flex items-center gap-3">
          <label className="flex cursor-pointer items-center gap-2 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={forYou}
              onChange={(e) => setForYou(e.target.checked)}
              className="h-4 w-4 accent-orange-600"
            />
            For You
          </label>
          <input
            type="search"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            maxLength={SEARCH_MAX}
            placeholder="Search dishes…"
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none sm:w-64"
          />
        </div>
      </div>

      <div className="mb-5 flex flex-wrap gap-2">
        {CATEGORIES.map((category) => {
          const active = selectedCategories.includes(category)
          return (
            <button
              key={category}
              onClick={() => toggleCategory(category)}
              className={`rounded-full border px-3 py-1 text-sm transition-colors ${
                active
                  ? 'border-orange-600 bg-orange-600 text-white'
                  : 'border-gray-300 bg-white text-gray-600 hover:border-orange-400'
              }`}
            >
              {category}
            </button>
          )
        })}
      </div>

      {isLoading && <p className="py-16 text-center text-sm text-gray-400">Loading menu…</p>}
      {isError && (
        <p className="py-16 text-center text-sm text-red-500">
          Failed to load the menu. Please try again.
        </p>
      )}
      {!isLoading && !isError && items.length === 0 && (
        <p className="py-16 text-center text-sm text-gray-400">No dishes match your search.</p>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {visibleItems.map((dish) => (
          <DishCard key={dish.dishId} dish={dish} highlighted={highlightSet.has(dish.dishId)} />
        ))}
      </div>

      {items.length > visibleCount && (
        <div className="mt-6 text-center">
          <button
            onClick={() => setVisibleCount((c) => c + PAGE_SIZE)}
            className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            Load more ({visibleCount} / {items.length})
          </button>
        </div>
      )}
    </div>
  )
}
