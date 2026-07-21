import { memo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { MenuItem } from '../api/menu'
import { formatYuan } from '../lib/money'
import { useAddToCart } from '../hooks/useAddToCart'

/** One menu card (PRD §3.2): image, name, price to the cent, cuisine tag, stock state. */
const DishCard = memo(function DishCard({ dish }: { dish: MenuItem }) {
  const navigate = useNavigate()
  const { requestAdd, adding, dialogNode } = useAddToCart()
  const soldOut = dish.stockRemaining === 0

  const onAdd = () => {
    if (dish.hasRequiredOptions) {
      navigate(`/dish/${dish.dishId}`)
      return
    }
    requestAdd({ dishId: dish.dishId, selectedOptions: [], qty: 1 })
  }

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      <Link to={`/dish/${dish.dishId}`} className="relative block">
        <img
          src={dish.imageUrl}
          alt={dish.name}
          loading="lazy"
          className={`aspect-[4/3] w-full object-cover ${soldOut ? 'opacity-40 grayscale' : ''}`}
        />
        {soldOut && (
          <span className="absolute inset-0 flex items-center justify-center text-lg font-semibold text-gray-700">
            Sold out
          </span>
        )}
      </Link>
      <div className="p-4">
        <div className="flex items-start justify-between gap-2">
          <Link
            to={`/dish/${dish.dishId}`}
            className="font-medium text-gray-900 hover:text-orange-600"
          >
            {dish.name}
          </Link>
          <span className="shrink-0 font-semibold text-orange-600">{formatYuan(dish.price)}</span>
        </div>
        <div className="mt-2 flex items-center justify-between">
          <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs text-gray-600">
            {dish.category}
          </span>
          <button
            onClick={onAdd}
            disabled={soldOut || adding}
            className="rounded-md bg-orange-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {soldOut ? 'Sold out' : dish.hasRequiredOptions ? 'Customize' : 'Add'}
          </button>
        </div>
      </div>
      {dialogNode}
    </div>
  )
})

export default DishCard
