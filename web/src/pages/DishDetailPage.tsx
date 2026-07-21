import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchDish } from '../api/menu'
import { formatCents, formatYuan, toCents } from '../lib/money'
import { useAddToCart } from '../hooks/useAddToCart'

/**
 * Dish detail (PRD §3.2): description, protein/allergen/spice info, customization groups
 * (required = pick exactly one, optional = pick any, extras may carry surcharges).
 * The price updates live as options change — all math in integer cents.
 */
export default function DishDetailPage() {
  const { id } = useParams<{ id: string }>()
  const dishId = Number(id)

  const { data: dish, isLoading, isError } = useQuery({
    queryKey: ['dish', dishId],
    queryFn: () => fetchDish(dishId),
    enabled: Number.isFinite(dishId),
  })

  // groupId -> selected itemIds (required groups hold at most one)
  const [selections, setSelections] = useState<Record<number, number[]>>({})
  const [qty, setQty] = useState(1)
  const { requestAdd, adding, dialogNode } = useAddToCart()

  const totalCents = useMemo(() => {
    if (!dish) return 0
    let cents = toCents(dish.price)
    for (const group of dish.optionGroups) {
      for (const itemId of selections[group.id] ?? []) {
        const item = group.items.find((i) => i.id === itemId)
        if (item) cents += toCents(item.extraPrice)
      }
    }
    return cents * qty
  }, [dish, selections, qty])

  if (isLoading) return <p className="py-16 text-center text-sm text-gray-400">Loading…</p>
  if (isError || !dish) {
    return (
      <p className="py-16 text-center text-sm text-red-500">
        Failed to load the dish. <Link to="/" className="text-orange-600 hover:underline">Back to menu</Link>
      </p>
    )
  }

  const requiredSatisfied = dish.optionGroups
    .filter((g) => g.required)
    .every((g) => (selections[g.id] ?? []).length === 1)

  const toggleItem = (groupId: number, itemId: number, required: boolean) => {
    setSelections((prev) => {
      const current = prev[groupId] ?? []
      const next = required
        ? current.includes(itemId) ? [] : [itemId]
        : current.includes(itemId)
          ? current.filter((v) => v !== itemId)
          : [...current, itemId]
      return { ...prev, [groupId]: next }
    })
  }

  const onAdd = () => {
    const selectedOptions = Object.entries(selections).flatMap(([groupId, itemIds]) =>
      itemIds.map((itemId) => ({ groupId: Number(groupId), itemId })),
    )
    requestAdd({ dishId: dish.dishId, selectedOptions, qty })
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Link to="/" className="text-sm text-gray-500 hover:text-orange-600">
        ← Back to menu
      </Link>
      <div className="mt-3 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <img src={dish.imageUrl} alt={dish.name} className="aspect-[16/9] w-full object-cover" />
        <div className="p-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-semibold text-gray-900">{dish.name}</h1>
              <div className="mt-2 flex flex-wrap gap-2 text-xs">
                <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-gray-600">
                  {dish.category}
                </span>
                <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-gray-600">
                  Protein: {dish.protein}
                </span>
                <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-gray-600">
                  Spice: {dish.spiceLevel}
                </span>
                {dish.allergens.length > 0 && (
                  <span className="rounded-full bg-amber-50 px-2.5 py-0.5 text-amber-700">
                    Allergens: {dish.allergens.join(', ')}
                  </span>
                )}
              </div>
            </div>
            <span className="shrink-0 text-xl font-semibold text-orange-600">
              {formatYuan(dish.price)}
            </span>
          </div>
          <p className="mt-4 text-sm leading-relaxed text-gray-600">{dish.description}</p>

          {dish.optionGroups.map((group) => (
            <div key={group.id} className="mt-6">
              <h2 className="mb-2 text-sm font-semibold text-gray-900">
                {group.name}{' '}
                <span className="font-normal text-gray-400">
                  {group.required ? '(choose 1)' : '(optional)'}
                </span>
              </h2>
              <div className="flex flex-wrap gap-2">
                {group.items.map((item) => {
                  const selected = (selections[group.id] ?? []).includes(item.id)
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => toggleItem(group.id, item.id, group.required)}
                      className={`rounded-full border px-3 py-1.5 text-sm transition-colors ${
                        selected
                          ? 'border-orange-600 bg-orange-50 text-orange-700'
                          : 'border-gray-300 bg-white text-gray-600 hover:border-orange-400'
                      }`}
                    >
                      {item.name}
                      {item.extraPrice > 0 && ` (+${formatYuan(item.extraPrice)})`}
                    </button>
                  )
                })}
              </div>
            </div>
          ))}

          <div className="mt-8 flex items-center justify-between border-t border-gray-100 pt-5">
            <div className="flex items-center gap-3">
              <button
                onClick={() => setQty((q) => Math.max(1, q - 1))}
                className="h-8 w-8 rounded-md border border-gray-300 text-gray-600 hover:bg-gray-50"
                aria-label="Decrease quantity"
              >
                −
              </button>
              <span className="w-6 text-center font-medium">{qty}</span>
              <button
                onClick={() => setQty((q) => Math.min(5, q + 1))}
                className="h-8 w-8 rounded-md border border-gray-300 text-gray-600 hover:bg-gray-50"
                aria-label="Increase quantity"
              >
                +
              </button>
            </div>
            <button
              onClick={onAdd}
              disabled={!requiredSatisfied || adding}
              className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:bg-gray-300"
            >
              Add to cart · {formatCents(totalCents)}
            </button>
          </div>
          {!requiredSatisfied && (
            <p className="mt-2 text-right text-xs text-gray-400">
              Please complete the required options first.
            </p>
          )}
        </div>
      </div>
      {dialogNode}
    </div>
  )
}
