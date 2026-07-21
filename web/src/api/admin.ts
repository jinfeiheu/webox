import { api } from './client'

export interface AdminDish {
  id: number
  name: string
  description: string
  price: number
  category: string
  protein: string
  spiceLevel: string
  allergens: string[]
  imageUrl: string
  active: boolean
}

export interface DishFormPayload {
  name: string
  description: string
  price: number
  category: string
  protein: string
  spiceLevel: string
  allergens: string[]
  imageUrl?: string
}

/** Subset of Spring's Page shape that the UI needs. */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export async function fetchAdminDishes(params: {
  q?: string
  category?: string
  page?: number
  size?: number
}): Promise<PageResult<AdminDish>> {
  const { data } = await api.get<PageResult<AdminDish>>('/admin/dishes', {
    params: {
      q: params.q || undefined,
      category: params.category || undefined,
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  })
  return data
}

export async function createDish(payload: DishFormPayload): Promise<AdminDish> {
  const { data } = await api.post<AdminDish>('/admin/dishes', payload)
  return data
}

export async function updateDish(id: number, payload: DishFormPayload): Promise<AdminDish> {
  const { data } = await api.put<AdminDish>(`/admin/dishes/${id}`, payload)
  return data
}

export async function setDishStatus(id: number, active: boolean): Promise<AdminDish> {
  const { data } = await api.patch<AdminDish>(`/admin/dishes/${id}/status`, { active })
  return data
}

export async function uploadDishImage(id: number, file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<{ imageUrl: string }>(`/admin/dishes/${id}/image`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data.imageUrl
}

export interface DailyMenuEntry {
  dishId: number
  name: string
  imageUrl: string
  category: string
  selected: boolean
  stockTotal: number
}

export interface DailyMenuAdmin {
  date: string
  entries: DailyMenuEntry[]
}

export async function fetchDailyMenuAdmin(date: string): Promise<DailyMenuAdmin> {
  const { data } = await api.get<DailyMenuAdmin>(`/admin/menus/${date}`)
  return data
}

export async function saveDailyMenu(
  date: string,
  entries: { dishId: number; selected: boolean; stockTotal: number }[],
): Promise<void> {
  await api.put(`/admin/menus/${date}`, { entries })
}
