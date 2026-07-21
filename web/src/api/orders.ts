import { api } from './client'
import type { CartItem, SelectedOption } from './cart'

export interface OrderItemView {
  dishId: number
  dishName: string
  unitPrice: number
  options: SelectedOption[]
  qty: number
  subtotal: number
}

export interface OrderView {
  orderId: number
  orderNo: string
  deliveryDate: string
  mealSlot: string
  address: string
  totalPrice: number
  status: string
  createdAt: string
  items: OrderItemView[]
}

export interface CheckoutSummary {
  /** Effective date/slot after cutoff auto-switch (PRD §4.2). */
  date: string
  slot: string
  /** True when the requested slot was past cutoff and got auto-switched. */
  switched: boolean
  items: CartItem[]
  totalQty: number
  totalPrice: number
  /** Existing active order for the effective slot, if any — UI shows "View Existing Order". */
  existingOrder: { orderId: number; orderNo: string } | null
}

export async function fetchCheckoutSummary(params: {
  date?: string
  slot?: string
}): Promise<CheckoutSummary> {
  const { data } = await api.get<CheckoutSummary>('/checkout/summary', {
    params: { date: params.date || undefined, slot: params.slot || undefined },
  })
  return data
}

export async function fetchAddresses(): Promise<string[]> {
  const { data } = await api.get<string[]>('/addresses')
  return data
}

export async function placeOrder(req: {
  deliveryDate: string
  mealSlot: string
  address: string
  idempotencyKey: string
}): Promise<OrderView> {
  const { data } = await api.post<OrderView>('/orders', req)
  return data
}
