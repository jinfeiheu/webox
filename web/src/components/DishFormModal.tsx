import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { createDish, updateDish, uploadDishImage } from '../api/admin'
import type { AdminDish, DishFormPayload } from '../api/admin'
import { ALLERGENS, CATEGORIES, SPICE_LEVELS } from '../lib/constants'

interface Props {
  dish: AdminDish | null // null = create mode
  onClose: () => void
}

const EMPTY: DishFormPayload = {
  name: '',
  description: '',
  price: 0,
  category: 'Chinese',
  protein: 'None',
  spiceLevel: 'None',
  allergens: [],
}

/** Create/edit dish modal (PRD §4.3). Image upload is available once the dish exists. */
export default function DishFormModal({ dish, onClose }: Props) {
  const queryClient = useQueryClient()
  const editing = dish != null
  const [form, setForm] = useState<DishFormPayload>(
    dish
      ? {
          name: dish.name,
          description: dish.description,
          price: dish.price,
          category: dish.category,
          protein: dish.protein,
          spiceLevel: dish.spiceLevel,
          allergens: dish.allergens,
          imageUrl: dish.imageUrl,
        }
      : EMPTY,
  )
  const [imageUrl, setImageUrl] = useState(dish?.imageUrl ?? '')

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-dishes'] })

  const saveMutation = useMutation({
    mutationFn: () => (editing ? updateDish(dish!.id, form) : createDish(form)),
    onSuccess: () => {
      invalidate()
      toast.success(editing ? 'Dish updated' : 'Dish created')
      onClose()
    },
  })

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadDishImage(dish!.id, file),
    onSuccess: (url) => {
      setImageUrl(url)
      setForm((f) => ({ ...f, imageUrl: url }))
      invalidate()
      toast.success('Image uploaded')
    },
  })

  const toggleAllergen = (a: string) => {
    setForm((f) => ({
      ...f,
      allergens: f.allergens.includes(a)
        ? f.allergens.filter((x) => x !== a)
        : [...f.allergens, a],
    }))
  }

  const inputClass =
    'w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-orange-500 focus:outline-none'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-xl bg-white p-5 shadow-xl">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          {editing ? 'Edit dish' : 'Add dish'}
        </h2>

        <div className="space-y-3">
          <div>
            <label className="mb-1 block text-sm text-gray-600">Name</label>
            <input
              className={inputClass}
              value={form.name}
              maxLength={128}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>
          <div>
            <label className="mb-1 block text-sm text-gray-600">Description</label>
            <textarea
              className={inputClass}
              rows={2}
              maxLength={512}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-sm text-gray-600">Price (¥)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className={inputClass}
                value={form.price}
                onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">Protein</label>
              <input
                className={inputClass}
                value={form.protein}
                maxLength={64}
                onChange={(e) => setForm({ ...form, protein: e.target.value })}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">Category</label>
              <select
                className={inputClass}
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">Spice level</label>
              <select
                className={inputClass}
                value={form.spiceLevel}
                onChange={(e) => setForm({ ...form, spiceLevel: e.target.value })}
              >
                {SPICE_LEVELS.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm text-gray-600">Allergens</label>
            <div className="flex flex-wrap gap-1.5">
              {ALLERGENS.map((a) => {
                const active = form.allergens.includes(a)
                return (
                  <button
                    key={a}
                    type="button"
                    onClick={() => toggleAllergen(a)}
                    className={`rounded-full border px-2.5 py-0.5 text-xs ${
                      active
                        ? 'border-orange-600 bg-orange-600 text-white'
                        : 'border-gray-300 text-gray-600 hover:border-orange-400'
                    }`}
                  >
                    {a}
                  </button>
                )
              })}
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm text-gray-600">Image</label>
            {imageUrl && (
              <img src={imageUrl} alt="preview" className="mb-2 h-24 w-32 rounded object-cover" />
            )}
            {editing ? (
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const file = e.target.files?.[0]
                  if (file) uploadMutation.mutate(file)
                }}
                className="text-sm text-gray-600"
              />
            ) : (
              <p className="text-xs text-gray-400">Save the dish first, then upload an image.</p>
            )}
          </div>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            onClick={() => saveMutation.mutate()}
            disabled={saveMutation.isPending}
            className="rounded-md bg-orange-600 px-4 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
          >
            {saveMutation.isPending ? 'Saving…' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  )
}
