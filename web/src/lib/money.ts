/**
 * Money helpers (PRD §6): API prices arrive as JSON numbers with 2 decimals.
 * All arithmetic is done in integer cents to avoid floating-point drift;
 * only the final display value is divided back.
 */

export function toCents(amount: number): number {
  return Math.round(amount * 100)
}

/** Format an API amount (yuan) as ¥xx.xx — display only, never for math. */
export function formatYuan(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

/** Format integer cents as ¥xx.xx. */
export function formatCents(cents: number): string {
  return `¥${(cents / 100).toFixed(2)}`
}
