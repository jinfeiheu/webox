import { api } from './client'

export interface DashboardView {
  today: {
    totalOrders: number
    totalRevenue: number
    pending: number
    confirmed: number
    completed: number
    cancelled: number
  }
  topDishes: { dishId: number; dishName: string; totalSold: number }[]
  slots: { lunchCount: number; dinnerCount: number }
  trend: { date: string; orderCount: number; revenue: number }[]
  lowStock: { dishId: number; dishName: string; stockRemaining: number }[]
}

export async function fetchDashboard(): Promise<DashboardView> {
  const { data } = await api.get<DashboardView>('/admin/dashboard')
  return data
}
