import { Link, Navigate, useLocation } from 'react-router-dom'
import type { OrderView } from '../api/orders'

/** Order success page (PRD §3.4): order number + expected delivery info. */
export default function OrderSuccessPage() {
  const location = useLocation()
  const order = (location.state as { order?: OrderView } | null)?.order

  if (!order) {
    return <Navigate to="/orders" replace />
  }

  return (
    <div className="mx-auto max-w-lg py-10 text-center">
      <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-green-100 text-2xl text-green-700">
        ✓
      </div>
      <h1 className="text-2xl font-semibold text-gray-900">Order placed successfully!</h1>
      <p className="mt-2 text-sm text-gray-500">
        Order No. <span className="font-mono font-medium text-gray-900">{order.orderNo}</span>
      </p>

      <div className="mt-6 rounded-xl border border-gray-200 bg-white p-5 text-left">
        <h2 className="mb-3 text-sm font-semibold text-gray-900">Estimated delivery</h2>
        <dl className="space-y-2 text-sm">
          <div className="flex justify-between">
            <dt className="text-gray-500">Date</dt>
            <dd className="font-medium text-gray-900">{order.deliveryDate}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-gray-500">Meal slot</dt>
            <dd className="font-medium text-gray-900">{order.mealSlot}</dd>
          </div>
          <div className="flex justify-between gap-6">
            <dt className="shrink-0 text-gray-500">Address</dt>
            <dd className="text-right font-medium text-gray-900">{order.address}</dd>
          </div>
        </dl>
      </div>

      <div className="mt-6 flex justify-center gap-3">
        <Link
          to={`/orders/${order.orderId}`}
          className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700"
        >
          View order details
        </Link>
        <Link
          to="/"
          className="rounded-md border border-gray-300 px-5 py-2.5 text-sm text-gray-700 hover:bg-gray-50"
        >
          Back to menu
        </Link>
      </div>
    </div>
  )
}
