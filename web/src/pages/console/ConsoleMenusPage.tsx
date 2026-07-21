import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { fetchDailyMenuAdmin, saveDailyMenu } from '../../api/admin'
import type { DailyMenuEntry } from '../../api/admin'

function tomorrow(): string {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

/** Daily menu setup (PRD §4.3): pick which dishes appear on a date and set each day's supply. */
export default function ConsoleMenusPage() {
  const queryClient = useQueryClient()
  const [date, setDate] = useState(tomorrow)
  const [entries, setEntries] = useState<DailyMenuEntry[]>([])

  const { data, isLoading } = useQuery({
    queryKey: ['admin-menu', date],
    queryFn: () => fetchDailyMenuAdmin(date),
  })

  // Hydrate the editable copy whenever the loaded menu changes.
  useEffect(() => {
    if (data) setEntries(data.entries)
  }, [data])

  const saveMutation = useMutation({
    mutationFn: () =>
      saveDailyMenu(
        date,
        entries.map((e) => ({ dishId: e.dishId, selected: e.selected, stockTotal: e.stockTotal })),
      ),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-menu', date] })
      queryClient.invalidateQueries({ queryKey: ['menu'] })
      toast.success('Daily menu saved')
    },
  })

  const update = (dishId: number, patch: Partial<DailyMenuEntry>) => {
    setEntries((prev) => prev.map((e) => (e.dishId === dishId ? { ...e, ...patch } : e)))
  }

  const selectedCount = entries.filter((e) => e.selected).length

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">Daily Menu</h1>
        <div className="flex items-center gap-2">
          <label htmlFor="menu-date" className="text-sm text-gray-600">
            Date
          </label>
          <input
            id="menu-date"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
          />
        </div>
      </div>

      {isLoading && <p className="py-12 text-center text-sm text-gray-400">Loading…</p>}

      {!isLoading && (
        <>
          <p className="mb-3 text-sm text-gray-500">
            {selectedCount} of {entries.length} dishes selected for {date}.
          </p>
          <div className="space-y-2">
            {entries.map((entry) => (
              <div
                key={entry.dishId}
                className="flex items-center gap-3 rounded-lg border border-gray-200 bg-white p-3"
              >
                <input
                  type="checkbox"
                  checked={entry.selected}
                  onChange={(e) => update(entry.dishId, { selected: e.target.checked })}
                  className="h-4 w-4 accent-orange-600"
                />
                <img
                  src={entry.imageUrl}
                  alt={entry.name}
                  className="h-10 w-10 rounded object-cover"
                />
                <div className="min-w-0 flex-1">
                  <div className="truncate font-medium text-gray-900">{entry.name}</div>
                  <div className="text-xs text-gray-400">{entry.category}</div>
                </div>
                <label className="flex items-center gap-2 text-sm text-gray-600">
                  Stock
                  <input
                    type="number"
                    min={0}
                    value={entry.stockTotal}
                    disabled={!entry.selected}
                    onChange={(e) =>
                      update(entry.dishId, { stockTotal: Number(e.target.value) })
                    }
                    className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm focus:border-orange-500 focus:outline-none disabled:bg-gray-100 disabled:text-gray-400"
                  />
                </label>
              </div>
            ))}
          </div>

          <div className="mt-5 text-right">
            <button
              onClick={() => saveMutation.mutate()}
              disabled={saveMutation.isPending}
              className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
            >
              {saveMutation.isPending ? 'Saving…' : 'Save daily menu'}
            </button>
          </div>
        </>
      )}
    </div>
  )
}
