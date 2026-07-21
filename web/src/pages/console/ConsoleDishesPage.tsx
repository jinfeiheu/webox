import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchAdminDishes, setDishStatus } from '../../api/admin'
import type { AdminDish } from '../../api/admin'
import { CATEGORIES } from '../../lib/constants'
import { formatYuan } from '../../lib/money'
import DishFormModal from '../../components/DishFormModal'

/** Console dish management (PRD §4.3): searchable/filterable table, create/edit, on-off shelf. */
export default function ConsoleDishesPage() {
  const queryClient = useQueryClient()
  const [q, setQ] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<AdminDish | null>(null)
  const [creating, setCreating] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['admin-dishes', q, category, page],
    queryFn: () => fetchAdminDishes({ q, category, page, size: 10 }),
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => setDishStatus(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-dishes'] }),
  })

  const dishes = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">Dish Management</h1>
        <button
          onClick={() => setCreating(true)}
          className="rounded-md bg-orange-600 px-4 py-2 text-sm font-medium text-white hover:bg-orange-700"
        >
          + Add Dish
        </button>
      </div>

      <div className="mb-4 flex flex-col gap-2 sm:flex-row">
        <input
          type="search"
          value={q}
          onChange={(e) => {
            setQ(e.target.value)
            setPage(0)
          }}
          placeholder="Search dishes…"
          className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none sm:w-64"
        />
        <select
          value={category}
          onChange={(e) => {
            setCategory(e.target.value)
            setPage(0)
          }}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
        >
          <option value="">All categories</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
      </div>

      <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Dish</th>
              <th className="px-4 py-3">Category</th>
              <th className="px-4 py-3">Price</th>
              <th className="px-4 py-3">Spice</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {isLoading && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  Loading…
                </td>
              </tr>
            )}
            {!isLoading && dishes.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  No dishes found.
                </td>
              </tr>
            )}
            {dishes.map((dish) => (
              <tr key={dish.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <img
                      src={dish.imageUrl}
                      alt={dish.name}
                      className="h-10 w-10 rounded object-cover"
                    />
                    <div>
                      <div className="font-medium text-gray-900">{dish.name}</div>
                      <div className="text-xs text-gray-400">{dish.protein}</div>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3 text-gray-600">{dish.category}</td>
                <td className="px-4 py-3 text-gray-900">{formatYuan(dish.price)}</td>
                <td className="px-4 py-3 text-gray-600">{dish.spiceLevel}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={() =>
                      statusMutation.mutate({ id: dish.id, active: !dish.active })
                    }
                    className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      dish.active
                        ? 'bg-green-50 text-green-700 hover:bg-green-100'
                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                    }`}
                  >
                    {dish.active ? 'On shelf' : 'Off shelf'}
                  </button>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => setEditing(dish)}
                    className="text-sm font-medium text-orange-600 hover:underline"
                  >
                    Edit
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm text-gray-600">
          <span>
            Page {page + 1} / {totalPages} ({data?.totalElements ?? 0} dishes)
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="rounded-md border border-gray-300 px-3 py-1 disabled:opacity-40"
            >
              Prev
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="rounded-md border border-gray-300 px-3 py-1 disabled:opacity-40"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {(creating || editing) && (
        <DishFormModal dish={editing} onClose={() => {
          setCreating(false)
          setEditing(null)
        }} />
      )}
    </div>
  )
}
