import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchCart, removeCartItem, updateCartItemQty } from '../api/cart'
import { formatYuan } from '../lib/money'

const MAX_TOTAL_QTY = 5

/** Cart page (PRD §3.3): per-line config display, quantity stepper, remove, live totals. */
export default function CartPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data: cart, isLoading } = useQuery({ queryKey: ['cart'], queryFn: fetchCart })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['cart'] })

  const qtyMutation = useMutation({
    mutationFn: ({ id, qty }: { id: number; qty: number }) => updateCartItemQty(id, qty),
    onSuccess: invalidate,
  })
  const removeMutation = useMutation({
    mutationFn: (id: number) => removeCartItem(id),
    onSuccess: invalidate,
  })

  if (isLoading) return <p className="py-16 text-center text-sm text-gray-400">Loading cart…</p>

  const items = cart?.items ?? []
  const cartFull = (cart?.totalQty ?? 0) >= MAX_TOTAL_QTY

  if (items.length === 0) {
    return (
      <div className="py-16 text-center">
        <p className="text-gray-500">Your cart is empty.</p>
        <Link to="/" className="mt-2 inline-block text-sm font-medium text-orange-600 hover:underline">
          Browse today's menu
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="mb-4 text-2xl font-semibold text-gray-900">Cart</h1>

      {cartFull && (
        <div className="mb-4 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          An order can contain at most {MAX_TOTAL_QTY} items in total. Remove something to add
          more.
        </div>
      )}

      <div className="space-y-3">
        {items.map((item) => (
          <div
            key={item.cartItemId}
            className="flex gap-4 rounded-xl border border-gray-200 bg-white p-4"
          >
            <img
              src={item.imageUrl}
              alt={item.dishName}
              className="h-20 w-20 shrink-0 rounded-lg object-cover"
            />
            <div className="min-w-0 flex-1">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <div className="truncate font-medium text-gray-900">{item.dishName}</div>
                  {item.selectedOptions.length > 0 && (
                    <div className="mt-0.5 text-xs text-gray-500">
                      {item.selectedOptions.map((o) => o.itemName).join(' + ')}
                    </div>
                  )}
                  <div className="mt-1 text-sm text-gray-500">{formatYuan(item.unitPrice)} / item</div>
                </div>
                <div className="shrink-0 text-right">
                  <div className="font-semibold text-gray-900">{formatYuan(item.subtotal)}</div>
                </div>
              </div>
              <div className="mt-2 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => qtyMutation.mutate({ id: item.cartItemId, qty: item.qty - 1 })}
                    disabled={item.qty <= 1 || qtyMutation.isPending}
                    className="h-7 w-7 rounded-md border border-gray-300 text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                    aria-label="Decrease quantity"
                  >
                    −
                  </button>
                  <span className="w-5 text-center text-sm font-medium">{item.qty}</span>
                  <button
                    onClick={() => qtyMutation.mutate({ id: item.cartItemId, qty: item.qty + 1 })}
                    disabled={cartFull || qtyMutation.isPending}
                    title={cartFull ? `An order can contain at most ${MAX_TOTAL_QTY} items in total.` : undefined}
                    className="h-7 w-7 rounded-md border border-gray-300 text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                    aria-label="Increase quantity"
                  >
                    +
                  </button>
                </div>
                <button
                  onClick={() => removeMutation.mutate(item.cartItemId)}
                  disabled={removeMutation.isPending}
                  className="text-sm text-gray-400 hover:text-red-600"
                >
                  Remove
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 flex items-center justify-between rounded-xl border border-gray-200 bg-white p-4">
        <div className="text-sm text-gray-500">
          {cart?.totalQty} item(s)
          <span className="ml-3 text-base font-semibold text-gray-900">
            Total {formatYuan(cart?.totalPrice ?? 0)}
          </span>
        </div>
        <button
          onClick={() => navigate('/checkout')}
          className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700"
        >
          Go to Checkout
        </button>
      </div>
    </div>
  )
}
