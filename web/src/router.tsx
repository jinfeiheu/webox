import { createBrowserRouter, Navigate } from 'react-router-dom'
import RequireAuth from './components/RequireAuth'
import EmployeeLayout from './layouts/EmployeeLayout'
import ConsoleLayout from './layouts/ConsoleLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MenuPage from './pages/MenuPage'
import DishDetailPage from './pages/DishDetailPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrderSuccessPage from './pages/OrderSuccessPage'
import OrdersPage from './pages/OrdersPage'
import OrderDetailPage from './pages/OrderDetailPage'
import SettingsPage from './pages/SettingsPage'
import ConsoleDishesPage from './pages/console/ConsoleDishesPage'
import ConsoleMenusPage from './pages/console/ConsoleMenusPage'
import ConsoleDashboardPage from './pages/console/ConsoleDashboardPage'

/**
 * Route table. Employee pages share EmployeeLayout; Console pages share ConsoleLayout
 * and are ADMIN-only (RequireAuth role). Employees hitting /console bounce to '/'.
 */
export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    element: (
      <RequireAuth>
        <EmployeeLayout />
      </RequireAuth>
    ),
    children: [
      { path: '/', element: <MenuPage /> },
      { path: '/dish/:id', element: <DishDetailPage /> },
      { path: '/cart', element: <CartPage /> },
      { path: '/checkout', element: <CheckoutPage /> },
      { path: '/order-success', element: <OrderSuccessPage /> },
      { path: '/orders', element: <OrdersPage /> },
      { path: '/orders/:id', element: <OrderDetailPage /> },
      { path: '/settings', element: <SettingsPage /> },
    ],
  },
  {
    path: '/console',
    element: (
      <RequireAuth role="ADMIN">
        <ConsoleLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <Navigate to="/console/dishes" replace /> },
      { path: 'dishes', element: <ConsoleDishesPage /> },
      { path: 'menus', element: <ConsoleMenusPage /> },
      { path: 'dashboard', element: <ConsoleDashboardPage /> },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])
