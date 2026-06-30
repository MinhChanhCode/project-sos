import { baseApiFetch } from './BaseApiFetch'

const API_BASE = '/api/v1/carts'

export interface CartItemRequest {
  menuItemId: number
  quantity: number
  notes?: string
}

export interface CartResponse {
  id: number
  tableId: string
  tableName: string
  sessionId: string
  items: Array<{
    menuItemId: number
    name: string
    quantity: number
    unitPrice: number
    subtotal: number
  }>
  totalItems: number
  totalAmount: number
}

export const CartApi = {
  create: async (payload: { tableId: string; sessionId?: string }) => {
    const res = await baseApiFetch<any>(`${API_BASE}`, {
      method: 'POST',
      body: payload
    })
    return (res?.data ?? res) as CartResponse
  },

  getBySession: async (sessionId: string) => {
    const res = await baseApiFetch<any>(`${API_BASE}/session/${sessionId}`)
    return (res?.data ?? res) as CartResponse
  },

  getByTable: async (tableId: string) => {
    const res = await baseApiFetch<any>(`${API_BASE}/table/${tableId}`)
    return (res?.data ?? res) as CartResponse
  },

  openForTable: async (tableId: string) => {
    const res = await baseApiFetch<any>(`${API_BASE}/table/${tableId}/open`, { method: 'POST' })
    return (res?.data ?? res) as CartResponse
  },

  addItem: async (sessionId: string, data: CartItemRequest) => {
    const res = await baseApiFetch<CartResponse>(`${API_BASE}/session/${sessionId}/items`, {
      method: 'POST',
      body: data
    })
    return res
  },

  updateItem: async (sessionId: string, menuItemId: number, data: Partial<CartItemRequest>) => {
    const res = await baseApiFetch<CartResponse>(`${API_BASE}/session/${sessionId}/items/${menuItemId}`, {
      method: 'PUT',
      body: data
    })
    return res
  },

  removeItem: async (sessionId: string, menuItemId: number) => {
    const res = await baseApiFetch<CartResponse>(`${API_BASE}/session/${sessionId}/items/${menuItemId}`, {
      method: 'DELETE'
    })
    return res
  },

  clear: async (sessionId: string) => {
    const res = await baseApiFetch<CartResponse>(`${API_BASE}/session/${sessionId}/clear`, {
      method: 'DELETE'
    })
    return res
  },

  // Confirm order from session cart
  confirmOrder: async (sessionId: string) => {
    const res = await baseApiFetch<number>(`/api/v1/orders/session/${sessionId}/confirm`, {
      method: 'POST'
    })
    return res
  }
}


