import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { cancelOrder, fetchOrder } from '../api/orders'
import ConfirmDialog from '../components/ConfirmDialog'
import { formatYuan } from '../lib/money'
import { StatusChip } from './OrdersPage'

/** Order detail (PRD §3.4): line snapshots with options, totals, cancel (Pending only). */
export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)
  const queryClient = useQueryClient()
  const [confirmingCancel, setConfirmingCancel] = useState(false)

  const { data: order, isLoading, isError } = useQuery({
    queryKey: ['orders', orderId],
    queryFn: () => fetchOrder(orderId),
    enabled: Number.isFinite(orderId),
  })

  const cancelMutation = useMutation({
    mutationFn: () => cancelOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      toast.success('Order cancelled')
    },
    onSettled: () => setConfirmingCancel(false),
  })

  if (isLoading) return <p className="py-16 text-center text-sm text-gray-400">Loading…</p>
  if (isError || !order) {
    return (
      <p className="py-16 text-center text-sm text-red-500">
        Order not found.{' '}
        <Link to="/orders" className="text-orange-600 hover:underline">
          Back to my orders
        </Link>
      </p>
    )
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Link to="/orders" className="text-sm text-gray-500 hover:text-orange-600">
        ← Back to my orders
      </Link>

      <div className="mt-3 rounded-xl border border-gray-200 bg-white p-5">
        <div className="flex items-center justify-between gap-3">
          <h1 className="font-mono text-lg font-semibold text-gray-900">{order.orderNo}</h1>
          <StatusChip status={order.status} />
        </div>
        <p className="mt-1 text-sm text-gray-500">
          {order.deliveryDate} · {order.mealSlot} · {order.address}
        </p>

        <ul className="mt-4 divide-y divide-gray-100 border-t border-gray-100">
          {order.items.map((item, index) => (
            <li key={index} className="flex justify-between gap-4 py-3 text-sm">
              <div className="min-w-0">
                <span className="font-medium text-gray-900">
                  {item.dishName} × {item.qty}
                </span>
                {item.options.length > 0 && (
                  <div className="text-xs text-gray-500">
                    {item.options
                      .map((o) => (o.extraPrice > 0 ? `${o.itemName} (+${formatYuan(o.extraPrice)})` : o.itemName))
                      .join(' + ')}
                  </div>
                )}
                <div className="text-xs text-gray-400">{formatYuan(item.unitPrice)} / item</div>
              </div>
              <span className="shrink-0 text-gray-700">{formatYuan(item.subtotal)}</span>
            </li>
          ))}
        </ul>

        <div className="mt-3 flex justify-between border-t border-gray-100 pt-3">
          <span className="text-sm text-gray-500">Total</span>
          <span className="text-base font-semibold text-gray-900">
            {formatYuan(order.totalPrice)}
          </span>
        </div>
      </div>

      {order.status === 'Pending' && (
        <div className="mt-4 text-right">
          <button
            onClick={() => setConfirmingCancel(true)}
            className="rounded-md border border-red-300 px-4 py-2 text-sm text-red-600 hover:bg-red-50"
          >
            Cancel Order
          </button>
        </div>
      )}

      {confirmingCancel && (
        <ConfirmDialog
          title="Cancel order"
          message={`Cancel order ${order.orderNo}? This cannot be undone.`}
          confirmText="Cancel order"
          cancelText="Keep order"
          onConfirm={() => cancelMutation.mutate()}
          onCancel={() => setConfirmingCancel(false)}
        />
      )}
    </div>
  )
}
