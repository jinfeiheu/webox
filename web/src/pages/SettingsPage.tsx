import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { fetchPreferences, savePreferences } from '../api/preferences'
import type { PreferenceView } from '../api/preferences'
import { ALLERGENS, CATEGORIES, SPICE_LEVELS, TASTE_LEVELS } from '../lib/constants'

const EMPTY: PreferenceView = {
  allergens: [],
  cuisines: [],
  spiceLevel: null,
  taste: null,
  budgetMin: null,
  budgetMax: null,
}

function ChipToggle({
  label,
  active,
  onToggle,
}: {
  label: string
  active: boolean
  onToggle: () => void
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={`rounded-full border px-3 py-1 text-sm transition-colors ${
        active
          ? 'border-orange-600 bg-orange-600 text-white'
          : 'border-gray-300 bg-white text-gray-600 hover:border-orange-400'
      }`}
    >
      {label}
    </button>
  )
}

/** Preferences page (PRD §4.1): allergens, cuisine/spice/taste, per-meal budget. */
export default function SettingsPage() {
  const queryClient = useQueryClient()
  const { data: prefs } = useQuery({ queryKey: ['preferences'], queryFn: fetchPreferences })
  const [form, setForm] = useState<PreferenceView>(EMPTY)

  // Hydrate the form once preferences arrive (not on every cache change).
  useEffect(() => {
    if (prefs) setForm(prefs)
  }, [prefs])

  const saveMutation = useMutation({
    mutationFn: () => savePreferences(form),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] })
      toast.success('Preferences saved')
    },
  })

  const toggle = (key: 'allergens' | 'cuisines', value: string) => {
    setForm((prev) => {
      const list = prev[key]
      return {
        ...prev,
        [key]: list.includes(value) ? list.filter((v) => v !== value) : [...list, value],
      }
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-5 text-2xl font-semibold text-gray-900">Preferences</h1>

      <section className="rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-900">Allergens</h2>
        <p className="mt-1 text-xs text-gray-400">
          You'll be warned (not blocked) when adding a dish that contains a flagged allergen.
        </p>
        <div className="mt-3 flex flex-wrap gap-2">
          {ALLERGENS.map((a) => (
            <ChipToggle
              key={a}
              label={a}
              active={form.allergens.includes(a)}
              onToggle={() => toggle('allergens', a)}
            />
          ))}
        </div>
      </section>

      <section className="mt-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-900">Cuisine preferences</h2>
        <p className="mt-1 text-xs text-gray-400">Boosted to the top of the menu when "For You" is on.</p>
        <div className="mt-3 flex flex-wrap gap-2">
          {CATEGORIES.map((c) => (
            <ChipToggle
              key={c}
              label={c}
              active={form.cuisines.includes(c)}
              onToggle={() => toggle('cuisines', c)}
            />
          ))}
        </div>
      </section>

      <section className="mt-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-900">Taste</h2>
        <div className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="spice" className="mb-1 block text-sm text-gray-600">
              Spice level
            </label>
            <select
              id="spice"
              value={form.spiceLevel ?? ''}
              onChange={(e) => setForm({ ...form, spiceLevel: e.target.value || null })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            >
              <option value="">No preference</option>
              {SPICE_LEVELS.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="taste" className="mb-1 block text-sm text-gray-600">
              Taste richness
            </label>
            <select
              id="taste"
              value={form.taste ?? ''}
              onChange={(e) => setForm({ ...form, taste: e.target.value || null })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            >
              <option value="">No preference</option>
              {TASTE_LEVELS.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
        </div>
      </section>

      <section className="mt-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-900">Per-meal budget (¥)</h2>
        <p className="mt-1 text-xs text-gray-400">You'll be reminded at checkout if the total exceeds the cap.</p>
        <div className="mt-3 grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="budget-min" className="mb-1 block text-sm text-gray-600">
              Minimum
            </label>
            <input
              id="budget-min"
              type="number"
              min={0}
              step="0.01"
              value={form.budgetMin ?? ''}
              onChange={(e) =>
                setForm({ ...form, budgetMin: e.target.value === '' ? null : Number(e.target.value) })
              }
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="budget-max" className="mb-1 block text-sm text-gray-600">
              Maximum
            </label>
            <input
              id="budget-max"
              type="number"
              min={0}
              step="0.01"
              value={form.budgetMax ?? ''}
              onChange={(e) =>
                setForm({ ...form, budgetMax: e.target.value === '' ? null : Number(e.target.value) })
              }
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none"
            />
          </div>
        </div>
      </section>

      <div className="mt-6 text-right">
        <button
          onClick={() => saveMutation.mutate()}
          disabled={saveMutation.isPending}
          className="rounded-md bg-orange-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
        >
          {saveMutation.isPending ? 'Saving…' : 'Save preferences'}
        </button>
      </div>
    </div>
  )
}
