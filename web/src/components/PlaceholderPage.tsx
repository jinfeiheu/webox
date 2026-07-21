interface Props {
  title: string
  note?: string
}

/** Placeholder page used by the route skeleton (T02); replaced by real pages in later tasks. */
export default function PlaceholderPage({ title, note }: Props) {
  return (
    <div className="rounded-xl border border-dashed border-gray-300 bg-white p-10 text-center">
      <h1 className="text-2xl font-semibold text-gray-900">{title}</h1>
      {note && <p className="mt-2 text-sm text-gray-400">{note}</p>}
    </div>
  )
}
