import { baseApiFetch } from './BaseApiFetch'

export interface TableOrderItemSummary {
  id: number
  menuItemId: number
  menuItemName: string
  pendingQuantity?: number
  preparingQuantity?: number
  completedQuantity?: number
  servedQuantity?: number
  cancelledQuantity?: number
  outOfStockQuantity?: number
  totalQuantity?: number
  unitPrice: number
}

export interface TableDetailResponse {
  tableId: string
  tableName: string
  customers: number
  status: string
  totalAmount: number
  activeOrderId?: number
  sessionItems: TableOrderItemSummary[]
}

export interface TableListItemResponse {
  id: string
  name: string
  tableNumber?: number
  capacity: number
  isAvailable: boolean
  status?: string
  totalAmount?: number
  activeOrderId?: number
  areaId?: number
  posX?: number
  posY?: number
  tableStatus?: string
}

export const TableApi = {
  list: async () => {
    const res = await baseApiFetch<any>(`/api/v1/tables`)
    return (res?.data ?? res) as TableListItemResponse[]
  },
  getDetail: async (tableId: string) => {
    const res = await baseApiFetch<any>(`/api/v1/tables/${tableId}/detail`)
    return (res?.data ?? res) as TableDetailResponse
  },
  create: (body: { name: string; capacity: number; areaId?: number; posX?: number; posY?: number; tableStatus?: string }) =>
    baseApiFetch<any>(`/api/v1/tables`, { method: 'POST', body }).then(r => r?.data ?? r),
  update: (tableId: string, body: Partial<{ name: string; capacity: number; areaId: number; posX: number; posY: number; tableStatus: string }>) =>
    baseApiFetch<any>(`/api/v1/tables/${tableId}`, { method: 'PUT', body }).then(r => r?.data ?? r),
  updatePositions: (positions: { id: string; posX: number; posY: number }[]) =>
    baseApiFetch<void>(`/api/v1/tables/positions`, { method: 'PATCH', body: { positions } }),
  updateStatus: (tableId: string, status: string) =>
    baseApiFetch<any>(`/api/v1/tables/${tableId}/status?status=${status}`, { method: 'PATCH' }).then(r => r?.data ?? r),
  delete: (tableId: string) =>
    baseApiFetch<void>(`/api/v1/tables/${tableId}`, { method: 'DELETE' }),
  clear: (tableId: string) => baseApiFetch<void>(`/api/v1/tables/${tableId}/clear`, { method: 'PATCH' })
}
