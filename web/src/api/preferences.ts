import { api } from './client'

export interface PreferenceView {
  allergens: string[]
  cuisines: string[]
  spiceLevel: string | null
  taste: string | null
  budgetMin: number | null
  budgetMax: number | null
}

export async function fetchPreferences(): Promise<PreferenceView> {
  const { data } = await api.get<PreferenceView>('/preferences')
  return data
}

export async function savePreferences(pref: PreferenceView): Promise<PreferenceView> {
  const { data } = await api.put<PreferenceView>('/preferences', pref)
  return data
}
