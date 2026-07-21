import { api } from './client'

export interface MenuItem {
  dishId: number
  name: string
  description: string
  price: number
  category: string
  protein: string
  spiceLevel: string
  allergens: string[]
  imageUrl: string
  stockRemaining: number
  hasRequiredOptions: boolean
}

export interface MenuResponse {
  date: string
  items: MenuItem[]
}

export interface OptionItem {
  id: number
  name: string
  extraPrice: number
}

export interface OptionGroup {
  id: number
  name: string
  required: boolean
  items: OptionItem[]
}

export interface DishDetail {
  dishId: number
  name: string
  description: string
  price: number
  category: string
  protein: string
  spiceLevel: string
  allergens: string[]
  imageUrl: string
  optionGroups: OptionGroup[]
}

export async function fetchMenu(date?: string): Promise<MenuResponse> {
  const { data } = await api.get<MenuResponse>('/menu', { params: { date } })
  return data
}

export async function searchMenu(params: {
  q?: string
  categories?: string[]
  date?: string
}): Promise<MenuResponse> {
  const { data } = await api.get<MenuResponse>('/dishes/search', {
    params: {
      q: params.q || undefined,
      categories: params.categories?.length ? params.categories.join(',') : undefined,
      date: params.date,
    },
  })
  return data
}

export async function fetchDish(dishId: number): Promise<DishDetail> {
  const { data } = await api.get<DishDetail>(`/dishes/${dishId}`)
  return data
}
