import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchOrders } from '../api/orders'
import { formatYuan } from '../lib/money'

const STATUS_STYLES: Record<string, string> = {
  Pending: 'bg-amber-50 text-amber-700',
  Confirmed: 'bg-blue-50 text-blue-700',
  Completed: 'bg-green-50 text-green-700',
  Cancelled: 'bg-gray-100 text-gray-500',
}

export function StatusChip({ status }: { status: string }) {
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_STYLES[status] ?? 'bg-gray-100 text-gray-500'}`}
    >
      {status}
    </span>
  )
}

/** "My Orders" (PRD §3.4): all historical orders, newest first. */
export default function OrdersPage() {
  const { data: orders, isLoading } = useQuery({ queryKey: ['orders'], queryFn: fetchOrders })

  if (isLoading) return <p className="py-16 text-center text-sm text-gray-400">Loading orders…</p>

  if (!orders || orders.length === 0) {
    return (
      <div className="py-16 text-center">
        <p className="text-gray-500">No orders yet.</p>
        <Link to="/" className="mt-2 inline-block text-sm font-medium text-orange-600 hover:underline">
          Browse today's menu
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="mb-4 text-2xl font-semibold text-gray-900">My Orders</h1>
      <div className="space-y-3">
        {orders.map((order) => (
          <Link
            key={order.orderId}
            to={`/orders/${order.orderId}`}
            className="block rounded-xl border border-gray-200 bg-white p-4 transition-shadow hover:shadow-md"
          >
            <div className="flex items-center justify-between gap-3">
              <div className="min-w-0">
                <span className="font-mono text-sm font-medium text-gray-900">{order.orderNo}</span>
                <span className="ml-3 text-sm text-gray-500">
                  {order.deliveryDate} · {order.mealSlot}
                </span>
              </div>
              <StatusChip status={order.status} />
            </div>
            <div className="mt-2 flex justify-between text-sm">
              <span className="text-gray-500">{order.itemCount} item(s)</span>
              <span className="font-semibold text-gray-900">{formatYuan(order.totalPrice)}</span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
