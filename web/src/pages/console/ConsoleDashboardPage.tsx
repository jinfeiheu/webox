import { useQuery } from '@tanstack/react-query'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, LineChart, Line, Legend, ResponsiveContainer } from 'recharts'
import { fetchDashboard } from '../../api/dashboard'
import { formatYuan } from '../../lib/money'

/** Business dashboard (PRD §5.3): today overview, top-10 bar chart, lunch-vs-dinner, 7-day trend, low-stock. */
export default function ConsoleDashboardPage() {
  const { data, isLoading } = useQuery({ queryKey: ['dashboard'], queryFn: fetchDashboard })

  if (isLoading) return <p className="py-12 text-center text-sm text-gray-400">Loading…</p>
  if (!data) return <p className="py-12 text-center text-sm text-red-500">Failed to load dashboard.</p>

  const { today, topDishes, slots, trend, lowStock } = data

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-gray-900">Business Dashboard</h1>

      {/* Today overview — stat cards */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <StatCard label="Orders" value={today.totalOrders} />
        <StatCard label="Revenue" value={formatYuan(today.totalRevenue)} />
        <StatCard label="Pending" value={today.pending} color="amber" />
        <StatCard label="Confirmed" value={today.confirmed} color="blue" />
        <StatCard label="Completed" value={today.completed} color="green" />
        <StatCard label="Cancelled" value={today.cancelled} color="gray" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Top 10 dishes (bar chart) */}
        <div className="rounded-xl border border-gray-200 bg-white p-5">
          <h2 className="mb-3 text-sm font-semibold text-gray-900">Top 10 Dishes (all time)</h2>
          {topDishes.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">No data yet.</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={topDishes} layout="vertical" margin={{ left: 70, right: 10 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" />
                <YAxis type="category" dataKey="dishName" tick={{ fontSize: 12 }} width={80} />
                <Tooltip />
                <Bar dataKey="totalSold" fill="#ea580c" name="Sold" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Lunch vs dinner + 7-day trend */}
        <div className="space-y-6">
          <div className="rounded-xl border border-gray-200 bg-white p-5">
            <h2 className="mb-3 text-sm font-semibold text-gray-900">Meal Slot (today)</h2>
            <div className="flex items-center gap-8 text-sm">
              <div><span className="text-gray-500">Lunch</span> <span className="ml-1 text-lg font-semibold text-gray-900">{slots.lunchCount}</span></div>
              <div><span className="text-gray-500">Dinner</span> <span className="ml-1 text-lg font-semibold text-gray-900">{slots.dinnerCount}</span></div>
            </div>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-5">
            <h2 className="mb-3 text-sm font-semibold text-gray-900">7-Day Trend</h2>
            <ResponsiveContainer width="100%" height={180}>
              <LineChart data={trend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis yAxisId="left" tick={{ fontSize: 11 }} />
                <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 11 }} />
                <Tooltip />
                <Legend />
                <Line yAxisId="left" type="monotone" dataKey="orderCount" stroke="#ea580c" name="Orders" strokeWidth={2} />
                <Line yAxisId="right" type="monotone" dataKey="revenue" stroke="#2563eb" name="Revenue (¥)" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Low-stock warning */}
      <div className="rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-900">Low Stock Alert (≤ 3 today)</h2>
        {lowStock.length === 0 ? (
          <p className="text-sm text-gray-400">All stocked dishes are above the threshold.</p>
        ) : (
          <ul className="space-y-1 text-sm">
            {lowStock.map((item) => (
              <li key={item.dishId} className="flex items-center gap-2">
                <span className="inline-block h-2 w-2 rounded-full bg-red-500" />
                <span className="text-gray-900">{item.dishName}</span>
                <span className="text-red-600 font-medium">{item.stockRemaining} left</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

function StatCard({ label, value, color }: { label: string; value: string | number; color?: string }) {
  const colorClass =
    color === 'amber' ? 'text-amber-700 bg-amber-50' :
    color === 'blue' ? 'text-blue-700 bg-blue-50' :
    color === 'green' ? 'text-green-700 bg-green-50' :
    color === 'gray' ? 'text-gray-500 bg-gray-100' :
    'text-gray-900'
  return (
    <div className={`rounded-xl px-4 py-3 ${color === 'gray' ? 'bg-gray-100' : color === 'amber' ? 'bg-amber-50' : color === 'blue' ? 'bg-blue-50' : color === 'green' ? 'bg-green-50' : 'bg-white border border-gray-200'}`}>
      <div className="text-xs text-gray-400">{label}</div>
      <div className={`mt-1 text-lg font-semibold ${colorClass.split(' ')[0]}`}>{value}</div>
    </div>
  )
}
