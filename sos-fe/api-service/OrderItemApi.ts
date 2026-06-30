import { baseApiFetch } from "./BaseApiFetch";

const API_BASE = "/api/v1/order-items";

export interface OrderItemStatusResponse {
  code: string;
  name: string;
  description?: string;
  color?: string;
}

export interface OrderItemResponse {
  id: number;
  menuItemId: number;
  menuItemName: string;
  pendingQuantity?: number;
  preparingQuantity?: number;
  completedQuantity?: number; // Số lượng đã hoàn thành
  servedQuantity?: number;
  cancelledQuantity?: number;
  outOfStockQuantity?: number;
  totalQuantity?: number; // Tổng số lượng có tính tiền
  unitPrice: number;
  totalPrice: number;
  orderId?: number;
  tableName?: string;
  notes?: string; // Ghi chú cho món ăn
}

export interface UpdateOrderItemStatusRequest {
  status: string;
  completedQuantity?: number; // Số lượng đã hoàn thành (optional)
  notes?: string;
}

export const OrderItemApi = {
  // Lấy danh sách trạng thái món ăn
  getStatuses: async () => {
    const res = await baseApiFetch<any>(`${API_BASE}/statuses`)
    return (res?.data ?? res) as OrderItemStatusResponse[]
  },

  // Lấy danh sách món trong đơn hàng theo orderId
  getByOrderId: async (orderId: number) => {
    const res = await baseApiFetch<any>(`${API_BASE}/order/${orderId}`)
    return (res?.data ?? res) as OrderItemResponse[]
  },

  // Lấy thông tin món theo orderItemId
  getById: async (orderItemId: number) => {
    const res = await baseApiFetch<any>(`${API_BASE}/${orderItemId}`)
    return (res?.data ?? res) as OrderItemResponse
  },

  // Lấy tất cả món cần quản lý trạng thái
  getPendingForManagement: async () => {
    const res = await baseApiFetch<any>(`${API_BASE}/pending-for-management`)
    return (res?.data ?? res) as OrderItemResponse[]
  },

  // Cập nhật trạng thái món (với request body)
  updateStatus: (orderItemId: number, data: UpdateOrderItemStatusRequest) =>
    baseApiFetch(`${API_BASE}/${orderItemId}/status`, {
      method: "PATCH",
      body: data,
    }),

  // Cập nhật trạng thái món nhanh (với query param)
  updateStatusQuick: (orderItemId: number, status: string) => {
    const url = `${API_BASE}/${orderItemId}/status/quick?status=${status}`
    return baseApiFetch(url, {
      method: "PATCH",
    })
  },

  // Cập nhật trạng thái món theo số lượng cụ thể
  updateStatusPartial: (orderItemId: number, status: string, quantity: number) =>
    baseApiFetch(`${API_BASE}/${orderItemId}/status/partial?status=${status}&quantity=${quantity}`, {
      method: "PATCH",
    }),

  // Cập nhật trạng thái nhiều món cùng lúc
  updateMultipleStatusesQuick: (orderItemIds: number[], status: string) =>
    baseApiFetch(`${API_BASE}/batch/status/quick?orderItemIds=${orderItemIds.join(',')}&status=${status}`, {
      method: "PATCH",
    }),

  // Thêm món mới vào order với logic merge tự động
  addItemToOrder: (orderId: number, menuItemId: number, quantity: number, notes?: string) =>
    baseApiFetch(`${API_BASE}/order/${orderId}/add-item?menuItemId=${menuItemId}&quantity=${quantity}${notes ? `&notes=${encodeURIComponent(notes)}` : ''}`, {
      method: "POST",
    }),

  // Merge các order items duplicate trong order
  mergeDuplicateOrderItems: (orderId: number) =>
    baseApiFetch(`${API_BASE}/order/${orderId}/merge-duplicates`, {
      method: "POST",
    }),
};


