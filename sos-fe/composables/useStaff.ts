import { useStaffStore } from '~/stores/staff'
import { TableApi } from '~/api-service'
import { ref, computed, onMounted } from 'vue'
import { useNuxtApp } from 'nuxt/app'
import { deriveOrderItemStatus } from '@/utils/formatters'
import type { toast as toastType } from 'vue3-toastify'
import { OrderItemApi } from '@/api-service'
import { limitFloorPlanTables } from '~/utils/tableLimits'

export const useStaff = () => {
  // Store
  const staffStore = useStaffStore()

  // Reactive data
  interface Table {
    id: string
    number: string
    status: "trống" | "đang đặt" | "chờ phục vụ" | "đang ăn" | "thanh toán" | "served" | "ready" | "preparing"
    customers: number
    assignedStaff: string
    totalAmount: number
    orders: Array<{
      id: string
      items: Array<{
        id: string
        name: string
        quantity: number
        price: number
        status: 'pending' | 'preparing' | 'ready' | 'served' | 'completed'
        needsKitchen: boolean
        notes?: string
      }>
      status: string
      orderTime: string
      orderNumber?: number
    }>
  }

  const selectedTable = ref<Table | null>(null)
  const showNotifications = ref(false)
  const showStaffChat = ref(false)
  const staffRole = ref('server')
  const activeTab = ref('tables')

  // Computed
  const showTableDetail = computed({
    get: () => !!selectedTable.value,
    set: (value) => {
      if (!value) selectedTable.value = null
    }
  })

  const staffStats = computed(() => ({
    tablesAssigned: staffStore.tables.length,
    ordersServed: staffStore.tables.reduce((sum, table) => {
      return sum + table.orders.reduce((orderSum, order) => {
        return orderSum + order.items.filter((item) => item.status === 'served' || item.status === 'completed').length
      }, 0)
    }, 0),
    pendingOrders: staffStore.tables.reduce((sum, table) => {
      return sum + table.orders.reduce((orderSum, order) => {
        return orderSum + order.items.filter((item) => ['pending', 'preparing'].includes(item.status)).length
      }, 0)
    }, 0),
    averageRating: 0
  }))

  // Load tables from backend on first use
  const ensureTablesLoaded = async () => {
    try {
      const list = await TableApi.list()
      // Map backend tables to store format (keep existing shape)
      const tables = Array.isArray(list) ? list : ((list as any)?.data && Array.isArray((list as any).data) ? (list as any).data : [])
      const mapped = limitFloorPlanTables(tables as any[]).map((t: any) => ({
        id: String(t.id),
        number: t.name,
        status: t.isAvailable ? 'trống' as const : 'đang đặt',
        customers: t.capacity || 0,
        orders: [],
        assignedStaff: '',
        totalAmount: 0,
        posX: t.posX || 0,
        posY: t.posY || 0,
        tableStatus: t.tableStatus || 'EMPTY',
      }))
      ;(staffStore as any).tables = mapped

      // Hydrate chi tiết bàn + món trong bàn nếu có order đang mở
      await Promise.all(
        mapped.map(async (t: any) => {
          try {
            const detail = await TableApi.getDetail(String(t.id))
            const d = (detail as any)?.data ?? detail
            const target = (staffStore as any).tables.find((x: any) => x.id === String(t.id))
            if (!target) return
            target.totalAmount = Number(d?.totalAmount || 0)
            // Có order hiện tại -> nạp danh sách món
            if (d?.orderId) {
              const items = await OrderItemApi.getByOrderId(Number(d.orderId))
              const list = Array.isArray(items) ? items : ((items as any)?.data && Array.isArray((items as any).data) ? (items as any).data : [])
              const statusMap: Record<string, 'pending' | 'preparing' | 'ready' | 'served' | 'completed'> = {
                'PENDING': 'pending',
                'PREPARING': 'preparing',
                'COMPLETED': 'completed',
                'SERVED': 'served'
              }
              const orderItems = (list as any[]).map((it: any) => ({
                id: String(it.id),
                name: it.menuItemName,
                quantity: (it.totalQuantity ?? ((it.pendingQuantity || 0) + (it.preparingQuantity || 0) + (it.completedQuantity || 0) + (it.servedQuantity || 0))),
                price: Number(it.unitPrice),
                status: deriveOrderItemStatus(it).toLowerCase(),
                needsKitchen: true,
                notes: it.notes || ''
              }))
              target.orders = [{ 
                id: String(d.orderId), 
                items: orderItems, 
                status: 'pending', 
                orderTime: 'Giỏ hàng hiện tại',
                orderNumber: 1
              }]
            } else if (d?.sessionItems && d.sessionItems.length > 0) {
              // Nếu không có orderId nhưng có sessionItems (từ cart hoặc nhiều orders)
              const statusMap: Record<string, 'pending' | 'preparing' | 'ready' | 'served' | 'completed'> = {
                'PENDING': 'pending',
                'PREPARING': 'preparing',
                'COMPLETED': 'completed',
                'SERVED': 'served'
              }
              
              // Tạo một order duy nhất chứa tất cả items (không nhóm theo orderId)
              const allItems = d.sessionItems.map((item: any) => ({
                id: String(item.id),
                name: item.menuItemName,
                quantity: (item.totalQuantity ?? ((item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0))),
                price: Number(item.unitPrice),
                status: deriveOrderItemStatus(item).toLowerCase(),
                needsKitchen: true,
                notes: item.notes || '',
                orderTime: item.orderTime || 'Giỏ hàng hiện tại'
              }))
              
              
              
              // Sắp xếp items theo thời gian order (món order trước hiển thị trên)
              const sortedItems = allItems.sort((a: any, b: any) => {
                if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime !== 'Giỏ hàng hiện tại') return -1
                if (b.orderTime === 'Giỏ hàng hiện tại' && a.orderTime !== 'Giỏ hàng hiện tại') return 1
                if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime === 'Giỏ hàng hiện tại') return 0
                
                try {
                  const dateA = new Date(a.orderTime)
                  const dateB = new Date(b.orderTime)
                  return dateA.getTime() - dateB.getTime()
                } catch {
                  return 0
                }
              })
              
              // Tạo một order duy nhất chứa tất cả items đã sắp xếp
              target.orders = [{
                id: 'all-items',
                items: sortedItems,
                status: 'active',
                orderTime: 'Tất cả món'
              }]
              
              // Cập nhật trạng thái bàn dựa trên sessionItems
         if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'SERVED')) {
           target.status = 'đã phục vụ'
         } else if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'COMPLETED')) {
           target.status = 'sẵn sàng'
         } else if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'PREPARING')) {
           target.status = 'đang chế biến'
         } else {
           target.status = 'đang đặt'
         }
            }
          } catch {
            // ignore per table
          }
        })
      )
    } catch (_) {
      // keep demo data if backend not ready
    }
  }

  // Đăng ký nhận thông báo realtime: đơn mới cho bếp, món hoàn tất cho phục vụ
  const setupRealtimeSubscriptions = async () => {
    // Đảm bảo có danh sách bàn trước khi đăng ký
    await ensureTablesLoaded()
    // Sử dụng global seenOrderIds để tránh duplicate giữa realtime và polling
    if (!(window as any).__seenOrderIds) {
      (window as any).__seenOrderIds = new Set<string>()
    }
    const seenOrderIds = (window as any).__seenOrderIds
    
    try {
      const nuxt = useNuxtApp() as any
      if (!nuxt?.$realtime) return
      
      // Tránh đăng ký nhiều lần
      if ((window as any).__realtimeSubscribed) return
      ;(window as any).__realtimeSubscribed = true
      
      // Bếp: đơn mới
      await nuxt.$realtime.onKitchenOrders((msg: any) => {
        const toast = useNuxtApp().$toast as typeof toastType
        if (msg?.type === 'ORDER_CREATED') {
          const orderId = String(msg.orderId)
          // Kiểm tra đã thông báo chưa
          if (!seenOrderIds.has(orderId)) {
            seenOrderIds.add(orderId)
            toast.info(`Đơn mới cho bếp: ${msg.tableName || ('Bàn ' + msg.tableId)} (Order #${orderId})`)
            staffStore.addNotification({
              message: `Đơn mới - ${msg.tableName || ('Bàn ' + msg.tableId)}`,
              time: new Date().toLocaleTimeString(),
              type: 'order'
            })
            // Cập nhật lại dữ liệu bàn ngay lập tức
            if (msg.tableId) {
              refreshTable(String(msg.tableId))
            }
          }
        }
      })

      // Phục vụ: món hoàn tất
      await nuxt.$realtime.subscribe('/topic/server/orders-ready', (msg: any) => {
        const toast = useNuxtApp().$toast as typeof toastType
        if (msg?.status === 'COMPLETED') {
          // Tạo unique key cho item notification
          const itemKey = `${msg.orderItemId}-${msg.status}`
          if (!(window as any).__seenItemIds) {
            (window as any).__seenItemIds = new Set<string>()
          }
          
          if (!(window as any).__seenItemIds.has(itemKey)) {
            (window as any).__seenItemIds.add(itemKey)
            toast.success(`Món xong: ${msg.menuItemName || ('#' + msg.orderItemId)} tại ${msg.tableName || ('Bàn ' + msg.tableId)}`)
            staffStore.addNotification({
              message: `Món xong: ${msg.menuItemName || ('#' + msg.orderItemId)} - ${msg.tableName || ('Bàn ' + msg.tableId)}`,
              time: new Date().toLocaleTimeString(),
              type: 'kitchen'
            })
          }
        }
      })

      // Đăng ký theo dõi theo từng bàn để cập nhật realtime cho nhiều khách cùng bàn
      if (!(window as any).__staffSubscribedTableIds) {
        (window as any).__staffSubscribedTableIds = new Set<string>()
      }
      const subscribed: Set<string> = (window as any).__staffSubscribedTableIds

      const upsertOrderItemsToTable = (table: any, items: any[]) => {
        if (!Array.isArray(items)) return
        const allItems: any[] = [...(table.orders?.[0]?.items || [])]
        const map = new Map<string, any>()
        allItems.forEach(i => map.set(i.id, i))
        items.forEach((it: any) => {
          const q = {
            pending: it.pendingQuantity || 0,
            preparing: it.preparingQuantity || 0,
            completed: it.completedQuantity || 0,
            served: it.servedQuantity || 0
          }
          // upsert per-status virtual items
          const baseId = String(it.id)
          const pushIf = (status: string, qty: number) => {
            const vid = `${baseId}_${status}`
            if (qty > 0) {
              map.set(vid, {
                id: vid,
                name: it.menuItemName,
                quantity: qty,
                price: Number(it.unitPrice),
                status,
                needsKitchen: true,
                notes: it.notes || '',
                orderTime: it.orderTime || 'Giỏ hàng hiện tại'
              })
            } else {
              map.delete(vid)
            }
          }
          pushIf('pending', q.pending)
          pushIf('preparing', q.preparing)
          pushIf('completed', q.completed)
          pushIf('served', q.served)
        })
        const merged = Array.from(map.values())
        table.orders = [{ id: 'all-items', items: merged, status: 'active', orderTime: 'Tất cả món' }]
      }

      const subscribeForTables = async () => {
        const tables = (staffStore as any).tables || []
        for (const t of tables) {
          const id = String(t.id)
          if (subscribed.has(id)) continue
          subscribed.add(id)

          // Khi có order mới/được confirm ở bàn -> refresh bàn
          await nuxt.$realtime.subscribe(`/topic/tables/${id}/ordered`, (msg: any) => {
            if (msg?.type === 'ORDERED_UPDATED' && Array.isArray(msg.items)) {
              upsertOrderItemsToTable(t, msg.items)
            }
          })
          // Khi trạng thái món thay đổi -> refresh bàn
          await nuxt.$realtime.subscribe(`/topic/tables/${id}/order-items`, (msg: any) => {
            if (msg?.type === 'ORDER_ITEM_STATUS_CHANGED') {
              upsertOrderItemsToTable(t, [msg])
            }
          })
          // Sự kiện chung của bàn: trạng thái, dọn bàn...
          await nuxt.$realtime.subscribe(`/topic/tables/${id}`, (msg: any) => {
            if (msg?.type === 'TABLE_CLEARED') {
              t.orders = []
              t.status = 'trống'
            }
          })
        }
      }

      // Gọi ngay và thiết lập interval nhẹ để bắt kịp bàn mới được thêm/đổi ca
      await subscribeForTables()
      setInterval(subscribeForTables, 10000)
    } catch (_) { /* ignore */ }

    // Fallback polling: chỉ chạy nếu realtime không hoạt động
    try {
      const intervalMs = 10000 // Tăng interval để giảm tần suất check
      if (!(window as any).__staffPollingStarted) {
        ;(window as any).__staffPollingStarted = true
        
        // Đợi 5s trước khi bắt đầu polling để ưu tiên realtime
        setTimeout(() => {
          setInterval(async () => {
            try {
              // Chỉ chạy polling nếu realtime không hoạt động
              const nuxt = useNuxtApp() as any
              if (nuxt?.$realtime && (window as any).__realtimeSubscribed) {
                return // Skip polling nếu realtime đang hoạt động
              }
              
              const items = await OrderItemApi.getPendingForManagement()
              const list = Array.isArray(items) ? items : ((items as any)?.data && Array.isArray((items as any).data) ? (items as any).data : [])
              const orderIds = Array.from(new Set((list as any[]).map((i: any) => String(i.orderId)).filter(Boolean)))
              
              for (const oid of orderIds) {
                // nếu là đơn mới chưa thấy trước đây, tạo thông báo
                if (oid && !seenOrderIds.has(oid)) {
                  seenOrderIds.add(oid)
                  const toast = useNuxtApp().$toast as typeof toastType
                  toast.info(`Đơn mới (fallback): Order #${oid}`)
                  staffStore.addNotification({
                    message: `Đơn mới - Order #${oid}`,
                    time: new Date().toLocaleTimeString(),
                    type: 'order'
                  })
                }
              }
            } catch { /* ignore */ }
          }, intervalMs)
        }, 5000)
      }
    } catch { /* ignore */ }
  }

  // Refresh data for a specific table
  const refreshTable = async (tableId: string) => {
    try {
      const detail = await TableApi.getDetail(String(tableId))
      const d = (detail as any)?.data ?? detail
      const target = (staffStore as any).tables.find((x: any) => String(x.id) === String(tableId))
      if (!target) return

      target.totalAmount = Number(d?.totalAmount || 0)

      const statusMap: Record<string, 'pending' | 'preparing' | 'ready' | 'served' | 'completed'> = {
        'PENDING': 'pending',
        'PREPARING': 'preparing',
        'COMPLETED': 'completed',
        'SERVED': 'served'
      }

      if (d?.sessionItems && d.sessionItems.length > 0) {
        const allItems: any[] = []
        
        d.sessionItems.forEach((item: any) => {
          
          // Tạo item riêng cho từng trạng thái có quantity > 0
          const quantities = {
            pending: item.pendingQuantity || 0,
            preparing: item.preparingQuantity || 0,
            completed: item.completedQuantity || 0,
            served: item.servedQuantity || 0
          }
          
          // Tạo item cho pending
          if (quantities.pending > 0) {
            allItems.push({
              id: `${item.id}_pending`,
              name: item.menuItemName,
              quantity: quantities.pending,
              price: Number(item.unitPrice),
              status: 'pending',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại',
              orderId: item.orderId,
              orderIndex: 0, // Will be updated by parent
              itemIndex: allItems.length
            })
          }
          
          // Tạo item cho preparing
          if (quantities.preparing > 0) {
            allItems.push({
              id: `${item.id}_preparing`,
              name: item.menuItemName,
              quantity: quantities.preparing,
              price: Number(item.unitPrice),
              status: 'preparing',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại',
              orderId: item.orderId,
              orderIndex: 0, // Will be updated by parent
              itemIndex: allItems.length
            })
          }
          
          // Tạo item cho completed
          if (quantities.completed > 0) {
            allItems.push({
              id: `${item.id}_completed`,
              name: item.menuItemName,
              quantity: quantities.completed,
              price: Number(item.unitPrice),
              status: 'completed',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại',
              orderId: item.orderId,
              orderIndex: 0, // Will be updated by parent
              itemIndex: allItems.length
            })
          }
          
          // Tạo item cho served
          if (quantities.served > 0) {
            allItems.push({
              id: `${item.id}_served`,
              name: item.menuItemName,
              quantity: quantities.served,
              price: Number(item.unitPrice),
              status: 'served',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại',
              orderId: item.orderId,
              orderIndex: 0, // Will be updated by parent
              itemIndex: allItems.length
            })
          }
        })

        const sortedItems = allItems.sort((a: any, b: any) => {
          if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime !== 'Giỏ hàng hiện tại') return -1
          if (b.orderTime === 'Giỏ hàng hiện tại' && a.orderTime !== 'Giỏ hàng hiện tại') return 1
          if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime === 'Giỏ hàng hiện tại') return 0
          try {
            const dateA = new Date(a.orderTime)
            const dateB = new Date(b.orderTime)
            return dateA.getTime() - dateB.getTime()
          } catch {
            return 0
          }
        })

        target.orders = [{
          id: 'all-items',
          items: sortedItems,
          status: 'active',
          orderTime: 'Tất cả món'
        }]

        // Update overall table status
        if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'SERVED')) {
          target.status = 'đã phục vụ'
        } else if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'COMPLETED')) {
          target.status = 'sẵn sàng'
        } else if (d.sessionItems.some((item: any) => deriveOrderItemStatus(item) === 'PREPARING')) {
          target.status = 'đang chế biến'
        } else {
          target.status = 'đang đặt'
        }
      } else {
        target.orders = []
        target.status = d?.isAvailable ? 'trống' : 'đang đặt'
      }

      // Sync selection if viewing this table
      if (selectedTable.value && String(selectedTable.value.id) === String(tableId)) {
        selectedTable.value = { ...(target as any) }
      }
    } catch {
      // ignore per table
    }
  }

  // Lấy danh sách trạng thái món ăn
  const fetchOrderItemStatuses = async () => {
    try {
      const statuses = await OrderItemApi.getStatuses()
      return statuses
    } catch (error: any) {
      const toast = useNuxtApp().$toast as typeof toastType
      toast.error(`Lỗi tải trạng thái món: ${error.message || 'Không thể tải'}`)
      return []
    }
  }

  // Methods
  const getStatusTitle = (status: string) => {
    const titleMap: Record<string, string> = {
      'pending': 'Chờ xử lý',
      'preparing': 'Đang làm',
      'ready': 'Hoàn thành',
      'served': 'Đã phục vụ'
    }
    return titleMap[status] || status
  }

  const handleQuickAction = (action: 'kitchen' | 'drinks' | 'clean' | 'message') => {
    const toast = useNuxtApp().$toast as typeof toastType
    
    switch (action) {
      case 'kitchen':
        document.querySelector('[data-staff-order-status]')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
        toast.info('Đang mở danh sách trạng thái bếp')
        break
      case 'drinks':
        document.querySelector('[data-staff-order-status]')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
        toast.info('Đang lọc/xem nhanh đồ uống trong trạng thái món')
        break
      case 'clean':
        toast.info('Chọn bàn đã thanh toán trong sơ đồ rồi bấm Dọn bàn')
        break
      case 'message':
        showStaffChat.value = true
        break
    }
  }

  const handleTableSelect = async (table: Table) => {
    selectedTable.value = table
    try {
      await ensureTablesLoaded()
      const detail = await TableApi.getDetail(table.id)
      const d = (detail as any)?.data ?? detail
      // Cập nhật tổng tiền theo backend
      table.totalAmount = d.totalAmount

      // Tạo orders từ session items của bàn (bao gồm cả các lần order tiếp theo)
      if (d.sessionItems && d.sessionItems.length > 0) {
        const statusMap: Record<string, 'pending' | 'preparing' | 'ready' | 'served' | 'completed'> = {
          'PENDING': 'pending',
          'PREPARING': 'preparing',
          'COMPLETED': 'completed',
          'SERVED': 'served'
        }

        // Tạo items riêng biệt cho từng trạng thái có quantity > 0
        const allItems: any[] = []
        
        d.sessionItems.forEach((item: any) => {
          
          const quantities = {
            pending: item.pendingQuantity || 0,
            preparing: item.preparingQuantity || 0,
            completed: item.completedQuantity || 0,
            served: item.servedQuantity || 0
          }
          
          // Tạo item cho pending
          if (quantities.pending > 0) {
            allItems.push({
              id: `${item.id}_pending`,
              name: item.menuItemName,
              quantity: quantities.pending,
              price: Number(item.unitPrice),
              status: 'pending',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại'
            })
          }
          
          // Tạo item cho preparing
          if (quantities.preparing > 0) {
            allItems.push({
              id: `${item.id}_preparing`,
              name: item.menuItemName,
              quantity: quantities.preparing,
              price: Number(item.unitPrice),
              status: 'preparing',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại'
            })
          }
          
          // Tạo item cho completed
          if (quantities.completed > 0) {
            allItems.push({
              id: `${item.id}_completed`,
              name: item.menuItemName,
              quantity: quantities.completed,
              price: Number(item.unitPrice),
              status: 'completed',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại'
            })
          }
          
          // Tạo item cho served
          if (quantities.served > 0) {
            allItems.push({
              id: `${item.id}_served`,
              name: item.menuItemName,
              quantity: quantities.served,
              price: Number(item.unitPrice),
              status: 'served',
              needsKitchen: true,
              notes: item.notes || '',
              orderTime: item.orderTime || 'Giỏ hàng hiện tại'
            })
          }
        })
        
        // Sắp xếp items theo thời gian order (món order trước hiển thị trên)
        const sortedItems = allItems.sort((a: any, b: any) => {
          if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime !== 'Giỏ hàng hiện tại') return -1
          if (b.orderTime === 'Giỏ hàng hiện tại' && a.orderTime !== 'Giỏ hàng hiện tại') return 1
          if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime === 'Giỏ hàng hiện tại') return 0
          
          try {
            const dateA = new Date(a.orderTime)
            const dateB = new Date(b.orderTime)
            return dateA.getTime() - dateB.getTime()
          } catch {
            return 0
          }
        })
        
        // Tạo một order duy nhất chứa tất cả items đã sắp xếp
        table.orders = [{
          id: 'all-items',
          items: sortedItems,
          status: 'active',
          orderTime: 'Tất cả món'
        }]
      } else {
        table.orders = []
      }
    } catch (e) {
      // ignore for now
    }
  }

  const handleUpdateItemStatus = async (tableId: string, orderId: string, itemId: string, newStatus: 'pending' | 'preparing' | 'ready' | 'served' | 'completed') => {
    try {
      // Mapping trạng thái frontend sang backend
      // Map trạng thái FE sang enum backend
      const statusMapping: Record<string, string> = {
        pending: 'PENDING',
        preparing: 'PREPARING',
        ready: 'COMPLETED',
        served: 'SERVED',
        completed: 'SERVED'
      }
      
      const backendStatus = statusMapping[newStatus] || 'PENDING'
      
      // Gọi API để cập nhật trạng thái
      await OrderItemApi.updateStatusQuick(Number(itemId), backendStatus)
      
      // Cập nhật local store
      staffStore.updateItemStatus(tableId, orderId, itemId, newStatus)
      
      const toast = useNuxtApp().$toast as typeof toastType
      toast.success('Cập nhật thành công!\nTrạng thái món ăn đã được cập nhật')
    } catch (error: any) {
      const toast = useNuxtApp().$toast as typeof toastType
      toast.error(`Lỗi cập nhật trạng thái: ${error.message || 'Không thể cập nhật'}`)
    }
  }

  const handleChatCustomer = () => {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.info('Mở chat với khách hàng...')
  }

  const handleCompleteTable = () => {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.success('Hoàn thành bàn thành công!')
    selectedTable.value = null
  }

  // Function để reset notification state nếu cần
  const resetNotificationState = () => {
    (window as any).__seenOrderIds = new Set<string>()
    ;(window as any).__seenItemIds = new Set<string>()
    ;(window as any).__realtimeSubscribed = false
    ;(window as any).__staffPollingStarted = false
  }

  onMounted(() => {
    const nuxt = useNuxtApp() as any
    if (nuxt?.$realtime) {
      nuxt.$realtime.subscribe('/topic/management/tables', (msg: any) => {
        if (msg?.type === 'TABLE_STATUS_CHANGED') {
          ensureTablesLoaded()
        }
      })
    }
  })

  return {
    // Store
    staffStore,
    
    // Reactive data
    selectedTable,
    showNotifications,
    showStaffChat,
    staffRole,
    activeTab,
    
    // Computed
    showTableDetail,
    staffStats,
    
    // Methods
    handleQuickAction,
    ensureTablesLoaded,
    handleTableSelect,
    handleUpdateItemStatus,
    handleChatCustomer,
    handleCompleteTable,
    fetchOrderItemStatuses,
    setupRealtimeSubscriptions,
    resetNotificationState
  }
}
