import { api } from './client'

export interface SelectedOption {
  groupId: number
  groupName: string
  itemId: number
  itemName: string
  extraPrice: number
}

export interface CartItem {
  cartItemId: number
  dishId: number
  dishName: string
  imageUrl: string
  category: string
  unitPrice: number
  selectedOptions: SelectedOption[]
  qty: number
  subtotal: number
}

export interface CartView {
  items: CartItem[]
  totalQty: number
  totalPrice: number
}

export interface AddCartItemRequest {
  dishId: number
  selectedOptions: { groupId: number; itemId: number }[]
  qty: number
  /** Set after the employee confirms the allergen warning dialog (PRD §4.1). */
  confirmed?: boolean
}

export interface AddCartItemResponse {
  item: CartItem
  matchedAllergens: string[]
}

export async function fetchCart(): Promise<CartView> {
  const { data } = await api.get<CartView>('/cart')
  return data
}

export async function addCartItem(req: AddCartItemRequest): Promise<AddCartItemResponse> {
  const { data } = await api.post<AddCartItemResponse>('/cart/items', req)
  return data
}

export async function updateCartItemQty(cartItemId: number, qty: number): Promise<CartItem> {
  const { data } = await api.patch<CartItem>(`/cart/items/${cartItemId}`, { qty })
  return data
}

export async function removeCartItem(cartItemId: number): Promise<void> {
  await api.delete(`/cart/items/${cartItemId}`)
}
