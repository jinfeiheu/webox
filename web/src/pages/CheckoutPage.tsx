import { useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchAddresses, fetchCheckoutSummary, placeOrder } from '../api/orders'
import { fetchPreferences } from '../api/preferences'
import { formatYuan } from '../lib/money'
import { ADDRESS_MAX } from '../lib/validators'

/**
 * Checkout (PRD §3.4/§4.2): order lines with surcharges, delivery date/slot with cutoff
 * auto-switch transparency, address picker with history, idempotent Place Order —
 * one click generates one key; retries reuse it, and the button disables while in flight.
 */
export default function CheckoutPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [date, setDate] = useState<string | undefined>(undefined)
  const [slot, setSlot] = useState<string | undefined>(undefined)
  const [address, setAddress] = useState('')
  // One key per checkout visit: double-clicks and network retries all reuse it (PRD §3.4).
  const idempotencyKey = useRef(crypto.randomUUID())

  const { data: summary, isLoading } = useQuery({
    queryKey: ['checkout-summary', date, slot],
    queryFn: () => fetchCheckoutSummary({ date, slot }),
    placeholderData: keepPreviousData,
  })
  const { data: addresses } = useQuery({ queryKey: ['addresses'], queryFn: fetchAddresses })
  const { data: prefs } = useQuery({ queryKey: ['preferences'], queryFn: fetchPreferences })
  // PRD §4.1: remind — never block — when the total exceeds the per-meal budget cap.
  const overBudget =
    prefs?.budgetMax != null && summary ? summary.totalPrice > prefs.budgetMax : false

  const placeMutation = useMutation({
    mutationFn: () =>
      placeOrder({
        deliveryDate: summary!.date,
        mealSlot: summary!.slot,
        address: address.trim(),
        idempotencyKey: idempotencyKey.current,
      }),
    onSuccess: (order) => {
      idempotencyKey.current = crypto.randomUUID()
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      navigate('/order-success', { state: { order }, replace: true })
    },
  })

  if (isLoading && !summary) {
    return <p className="py-16 text-center text-sm text-gray-400">Loading checkout…</p>
  }
  if (!summary || summary.items.length === 0) {
    return (
      <div className="py-16 text-center">
        <p className="text-gray-500">Your cart is empty.</p>
        <Link to="/" className="mt-2 inline-block text-sm font-medium text-orange-600 hover:underline">
          Back to menu
        </Link>
      </div>
    )
  }

  const addressReady = address.trim().length > 0

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="mb-4 text-2xl font-semibold text-gray-900">Checkout</h1>

      {summary.switched && (
        <div className="mb-4 rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-800">
          The cutoff time for your requested slot has passed — your order was automatically
          moved to the nearest available slot: <strong>{summary.slot}</strong> on {summary.date}.
        </div>
      )}

      {overBudget && prefs?.budgetMax != null && (
        <div className="mb-4 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          This order ({formatYuan(summary!.totalPrice)}) exceeds your per-meal budget cap of{' '}
          {formatYuan(prefs.budgetMax)}. You can still place the order.
        </div>
      )}

      <div className="rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-900">Order items</h2>
        <ul className="divide-y divide-gray-100">
          {summary.items.map((item) => (
            <li key={item.cartItemId} className="flex justify-between gap-4 py-2.5 text-sm">
              <div className="min-w-0">
                <span className="font-medium text-gray-900">
                  {item.dishName} × {item.qty}
                </span>
                {item.selectedOptions.length > 0 && (
                  <div className="text-xs text-gray-500">
                    {item.selectedOptions
                      .map((o) => (o.extraPrice > 0 ? `${o.itemName} (+${formatYuan(o.extraPrice)})` : o.itemName))
                      .join(' + ')}
                  </div>
                )}
              </div>
              <span className="shrink-0 text-gray-700">{formatYuan(item.subtotal)}</span>
            </li>
          ))}
        </ul>
        <div className="mt-3 flex justify-between border-t border-gray-100 pt-3 text-sm">
          <span className="text-gray-500">{summary.totalQty} / 5 items</span>
          <span className="text-base font-semibold text-gray-900">
            Total {formatYuan(summary.totalPrice)}
          </span>
        </div>
      </div>

      <div className="mt-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-900">Delivery</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="delivery-date" className="mb-1 block text-sm text-gray-600">
              Delivery date
            </label>
            <input
              id="delivery-date"
              type="date"
              value={date ?? summary.date}
              min={summary.date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="meal-slot" className="mb-1 block text-sm text-gray-600">
              Meal slot <span className="text-xs text-gray-400">(cutoff: Lunch 10:00, Dinner 15:00)</span>
            </label>
            <select
              id="meal-slot"
              value={slot ?? summary.slot}
              onChange={(e) => setSlot(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            >
              <option value="Lunch">Lunch</option>
              <option value="Dinner">Dinner</option>
            </select>
          </div>
        </div>
        <div className="mt-4">
          <label htmlFor="address" className="mb-1 block text-sm text-gray-600">
            Delivery address
          </label>
          <input
            id="address"
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            maxLength={ADDRESS_MAX}
            placeholder="e.g. Building A, 3rd Floor, Desk 32"
            list="address-history"
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
          />
          <datalist id="address-history">
            {(addresses ?? []).map((a) => (
              <option key={a} value={a} />
            ))}
          </datalist>
        </div>
      </div>

      <div className="mt-5 flex items-center justify-between">
        <Link to="/cart" className="text-sm text-gray-500 hover:text-orange-600">
          ← Back to cart
        </Link>
        {summary.existingOrder ? (
          <button
            onClick={() => navigate(`/orders/${summary.existingOrder!.orderId}`)}
            className="rounded-md bg-gray-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-gray-800"
          >
            View Existing Order ({summary.existingOrder.orderNo})
          </button>
        ) : (
          <button
            onClick={() => placeMutation.mutate()}
            disabled={!addressReady || placeMutation.isPending}
            className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {placeMutation.isPending ? 'Placing order…' : 'Place Order'}
          </button>
        )}
      </div>
      {!addressReady && (
        <p className="mt-2 text-right text-xs text-gray-400">
          Please enter a delivery address to place the order.
        </p>
      )}
    </div>
  )
}
