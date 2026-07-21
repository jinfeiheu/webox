import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { addCartItem } from '../api/cart'
import type { AddCartItemRequest } from '../api/cart'
import ConfirmDialog from '../components/ConfirmDialog'

/**
 * Shared add-to-cart flow (PRD §3.3/§4.1): posts the item; if the server reports allergens
 * matching the employee's flags, shows the confirmation dialog and re-posts with
 * confirmed: true only after the employee opts in. Warn — never filter.
 */
export function useAddToCart() {
  const queryClient = useQueryClient()
  const [pending, setPending] = useState<{ req: AddCartItemRequest; allergens: string[] } | null>(
    null,
  )

  const mutation = useMutation({
    mutationFn: (req: AddCartItemRequest) => addCartItem(req),
    onSuccess: (res, req) => {
      if (res.matchedAllergens.length > 0 && !req.confirmed) {
        setPending({ req, allergens: res.matchedAllergens })
        return
      }
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      toast.success('Added to cart')
    },
  })

  const requestAdd = (req: AddCartItemRequest) => mutation.mutate(req)

  const dialogNode = pending ? (
    <ConfirmDialog
      title="Allergen warning"
      message={`This dish contains an allergen you flagged: [${pending.allergens.join(
        ', ',
      )}]. Add anyway?`}
      confirmText="Add anyway"
      onConfirm={() => {
        mutation.mutate({ ...pending.req, confirmed: true })
        setPending(null)
      }}
      onCancel={() => setPending(null)}
    />
  ) : null

  return { requestAdd, adding: mutation.isPending, dialogNode }
}
