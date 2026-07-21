import { useEffect, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchMenu, searchMenu } from '../api/menu'
import DishCard from '../components/DishCard'
import { SEARCH_MAX } from '../lib/validators'

const CATEGORIES = ['Chinese', 'Western', 'Japanese', 'Light Meal', 'Korean', 'Southeast Asian']
const PAGE_SIZE = 12

/** Home page (PRD §3.2): today's menu with multi-category filter, debounced search, pagination. */
export default function MenuPage() {
  const [keyword, setKeyword] = useState('')
  const [debouncedKeyword, setDebouncedKeyword] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

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

  // Reset pagination whenever the result set changes.
  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [queryKey.join('|')])

  const items = useMemo(() => data?.items ?? [], [data])
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
        <input
          type="search"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          maxLength={SEARCH_MAX}
          placeholder="Search dishes…"
          className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none sm:w-64"
        />
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
          <DishCard key={dish.dishId} dish={dish} />
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
