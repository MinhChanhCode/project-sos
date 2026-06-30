import { defineStore } from "pinia"

export interface Table {
  id: string
  number: string
  status: "trống" | "đang đặt" | "chờ phục vụ" | "đang ăn" | "thanh toán" | "đã phục vụ" | "sẵn sàng" | "đang chế biến"
  customers: number
  orders: Order[]
  assignedStaff: string
  totalAmount: number
}

export interface Order {
  id: string
  items: OrderItem[]
  status: "pending" | "preparing" | "ready" | "served"
  orderTime: string
  notes?: string
}

export interface OrderItem {
  id: string
  name: string
  quantity: number
  price: number
  status: "pending" | "preparing" | "ready" | "served" | "completed"
  needsKitchen: boolean
  notes?: string
}

export interface StaffMember {
  id: number
  name: string
  role: string
  tables: string[]
  rating: number
  ordersServed: number
  status: "active" | "break" | "offline"
}

export interface Notification {
  id: string
  message: string
  time: string  
  type: "kitchen" | "call" | "order"
  read?: boolean
}

export const useStaffStore = defineStore("staff", {
  state: () => ({
    tables: [] as Table[],

    staff: [] as StaffMember[],

    notifications: [] as Notification[],
  }),

  getters: {
    getTableById: (state) => (id: string) => state.tables.find((table) => table.id === id),
    getStaffById: (state) => (id: number) => state.staff.find((member) => member.id === id),
    activeStaff: (state) => state.staff.filter((member) => member.status === "active"),
    pendingNotifications: (state) => state.notifications.filter(n => !n.read).length,
  },

  actions: {
    updateItemStatus(tableId: string, orderId: string, itemId: string, newStatus: OrderItem["status"]) {
      const table = this.tables.find((t) => t.id === tableId)
      if (table) {
        const order = table.orders.find((o) => o.id === orderId)
        if (order) {
          const item = order.items.find((i) => i.id === itemId)
          if (item) {
            item.status = newStatus
          }
        }
      }
    },

    updateTableStatus(tableId: string, status: Table["status"]) {
      const table = this.tables.find((t) => t.id === tableId)
      if (table) {
        table.status = status
      }
    },

    addNotification(notification: Omit<Notification, "id" | "read">) {
      const newId = Date.now().toString()
      this.notifications.unshift({ ...notification, id: newId, read: false })
    },

    removeNotification(id: string) {
      const index = this.notifications.findIndex((n) => n.id === id)
      if (index > -1) {
        this.notifications.splice(index, 1)
      }
    },

    markAllRead() {
      this.notifications = this.notifications.map(n => ({ ...n, read: true }))
    },

    updateStaffStatus(staffId: number, status: StaffMember["status"]) {
      const staff = this.staff.find((s) => s.id === staffId)
      if (staff) {
        staff.status = status
      }
    },
  },
})
