<template>
  <div class="space-y-4">
         <div class="flex items-center justify-between">
      <h3 class="text-lg font-semibold">Quản lý trạng thái món</h3>
    </div>

    <!-- Ẩn khu vực quản lý/Thống kê theo yêu cầu -->

    <!-- Danh sách món cần cập nhật -->
    <div v-if="visibleOrderItems.length > 0">
      <h4 class="font-medium mb-3">{{ sectionTitle }}</h4>
      <div class="space-y-3">
        <UCard 
          v-for="item in visibleOrderItems" 
          :key="item.id"
          class="p-4"
        >
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-2">
                <h5 class="font-medium">{{ item.menuItemName }}</h5>
              </div>
              <div class="text-sm text-gray-600">
                Bàn: {{ item.tableName }} • Số lượng: {{ item.totalQuantity ?? (item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0) }} • 
                {{ formatPrice(item.unitPrice) }}
                <span v-if="item.orderTime" class="ml-2 text-blue-600">
                  • {{ formatOrderTime(item.orderTime) }}
                </span>
                <div class="mt-1 text-xs text-gray-500">
                  <span class="mr-3">Chờ xử lý: {{ item.pendingQuantity || 0 }}</span>
                  <span class="mr-3">Đang chế biến: {{ item.preparingQuantity || 0 }}</span>
                  <span class="mr-3">Hoàn tất: {{ item.completedQuantity || 0 }}</span>
                  <span class="mr-3">Đã phục vụ: {{ item.servedQuantity || 0 }}</span>
                  <span class="mr-3">Đã hủy: {{ item.cancelledQuantity || 0 }}</span>
                  <span>Hết món: {{ item.outOfStockQuantity || 0 }}</span>
                </div>
              </div>
                             <!-- Ghi chú -->
               <div v-if="item.notes && item.notes.trim() !== ''" class="text-sm text-blue-600 mt-1">
                 <Icon name="lucide:message-square" class="w-3 h-3 inline mr-1" />
                 Ghi chú: {{ item.notes }}
               </div>
               <!-- Hiển thị khi không có ghi chú -->
               <div v-else class="text-xs text-gray-400 mt-1 italic">
                 Không có ghi chú
               </div>

            </div>
            <div class="flex space-x-2">
              <!-- Dropdown số lượng để cập nhật từng phần -->
              <div class="flex items-center space-x-1">
                <span class="text-xs text-gray-500">Cập nhật:</span>
                <USelect
                  v-model="item.updateQuantity"
                  :options="getQuantityOptions(getGroupAvailable(item, (item.newStatus || item.currentStatus || 'PENDING') as string))"
                  placeholder="Tất cả"
                  size="xs"
                  class="w-20"
                />
                <span class="text-xs text-gray-500">/{{ getGroupAvailable(item, (item.newStatus || item.currentStatus || 'PENDING') as string) }}</span>
              </div>
              
              <USelect
                v-model="item.newStatus"
                :options="orderItemStatuses.map(s => ({ label: s.name, value: s.code }))"
                placeholder="Chọn trạng thái"
                size="sm"
                class="w-40"
                :disabled="item.currentStatus === 'SERVED'"
              />
              <UButton 
                @click="updateItemStatus(item)"
                size="sm"
                :disabled="item.currentStatus === 'SERVED' || !item.newStatus"
                :loading="item.updating"
              >
                Cập nhật
              </UButton>
            </div>
          </div>
        </UCard>
      </div>
    </div>

    <!-- Thông báo không có món nào -->
    <div v-else class="text-center py-8 text-gray-500">
      <Icon name="lucide:check-circle" class="w-12 h-12 mx-auto mb-3 text-green-500" />
      <p>Tất cả món đã được cập nhật trạng thái!</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { OrderItemApi, type OrderItemStatusResponse, type OrderItemResponse } from '@/api-service'
import { useNuxtApp } from 'nuxt/app'
import type { toast as toastType } from 'vue3-toastify'
import { deriveOrderItemStatus } from '@/utils/formatters'

// Props
  interface Props {
  tableId?: string
  orderId?: string
  tableNumber?: string
  focusType?: 'all' | 'kitchen' | 'drinks'
 }

const props = defineProps<Props>()

// Reactive data
const orderItemStatuses = ref<OrderItemStatusResponse[]>([])
const pendingOrderItems = ref<(OrderItemResponse & { 
  newStatus?: string, 
  updating?: boolean,
  tableName?: string,
  orderTime?: string,
  updateQuantity?: number | null,
  orderItemIds?: number[], // Thêm mảng chứa tất cả orderItemIds đã gộp
  // Lưu chi tiết số lượng theo từng cột trạng thái cho mỗi order item
  orderItemDetails?: Array<{id: number, pending: number, preparing: number, completed: number}>,
  currentStatus?: string
})[]>([])
const selectedStatus = ref<OrderItemStatusResponse | null>(null)
const loading = ref(false)

// Computed
const statusCounts = computed(() => {
  const counts: Record<string, number> = {}
  pendingOrderItems.value.forEach(item => {
    const s = item.currentStatus || deriveOrderItemStatus(item)
    counts[s] = (counts[s] || 0) + 1
  })
  return counts
})

const isDrinkItem = (item: any) => {
  const text = `${item.menuItemName || ""} ${item.categoryName || ""} ${item.type || ""}`.toLowerCase()
  return ["đồ uống", "do uong", "drink", "bia", "nước", "nuoc", "trà", "tra", "cafe", "cà phê", "cola", "sinh tố", "latte", "americano", "soda", "mocktail", "bạc xỉu", "bac xiu"].some((keyword) =>
    text.includes(keyword),
  )
}

const visibleOrderItems = computed(() => {
  if (props.focusType === 'drinks') return pendingOrderItems.value.filter((item) => isDrinkItem(item))
  if (props.focusType === 'kitchen') return pendingOrderItems.value.filter((item) => !isDrinkItem(item))
  return pendingOrderItems.value
})

const sectionTitle = computed(() => {
  if (props.focusType === 'drinks') return 'Đồ uống cần kiểm tra'
  if (props.focusType === 'kitchen') return 'Món bếp cần kiểm tra'
  return 'Món cần cập nhật trạng thái'
})

// Methods
const getGroupKey = (item: any) => {
  const idPart = item.orderId || item.tableName || 'N/A'
  return `${item.menuItemName}_${idPart}`
}

const fetchOrderItemStatuses = async () => {
  try {
    loading.value = true
    const statuses = await OrderItemApi.getStatuses()
    // Ẩn trạng thái SERVED ở khu vực quản lý
    orderItemStatuses.value = statuses.filter(s => s.code !== 'SERVED')
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(`Lỗi tải trạng thái món: ${error.message || 'Không thể tải'}`)
  } finally {
    loading.value = false
  }
}

const fetchPendingOrderItems = async () => {
  try {
    // Luôn lấy tất cả món cần quản lý trạng thái (không cần orderId cụ thể)
    const items = await OrderItemApi.getPendingForManagement()
    const list = Array.isArray(items) ? items : ((items as any)?.data && Array.isArray((items as any).data) ? (items as any).data : [])
    const filteredItems = (list as any[])
      .filter((item: any) => {
        const pending = Number(item.pendingQuantity || 0)
        const preparing = Number(item.preparingQuantity || 0)
        // Chỉ hiển thị khi còn pending hoặc preparing
        return pending > 0 || preparing > 0
      })
      .map((item: any) => ({
        ...item,
        currentStatus: deriveOrderItemStatus(item),
        newStatus: deriveOrderItemStatus(item),
        updating: false,
        tableName: item.tableName || 'N/A',
        orderTime: item.orderTime || null
      }))
    
    // Gộp món giống nhau theo tên + orderId (ưu tiên) hoặc tableName
    const groupedItems = new Map<string, any>()
    filteredItems.forEach(item => {
      const key = getGroupKey(item)
      if (groupedItems.has(key)) {
        const existing = groupedItems.get(key)!
        const addQty = (item.totalQuantity ?? ((item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0)))
        existing.quantity += addQty
        existing.totalPrice = existing.unitPrice * existing.quantity
        // Thêm orderItemId vào mảng
        if (!existing.orderItemIds) {
          existing.orderItemIds = [existing.id]
          existing.orderItemDetails = [{
            id: existing.id,
            pending: existing.pendingQuantity || 0,
            preparing: existing.preparingQuantity || 0,
            completed: existing.completedQuantity || 0
          }]
        }
        existing.orderItemIds.push(item.id)
        existing.orderItemDetails.push({
          id: item.id,
          pending: item.pendingQuantity || 0,
          preparing: item.preparingQuantity || 0,
          completed: item.completedQuantity || 0
        })
        // Gộp ghi chú nếu có
        if (item.notes && existing.notes !== item.notes) {
          existing.notes = existing.notes ? `${existing.notes}; ${item.notes}` : item.notes
        }
        // Lấy thời gian order sớm nhất
        if (item.orderTime && (!existing.orderTime || new Date(item.orderTime) < new Date(existing.orderTime))) {
          existing.orderTime = item.orderTime
        }
      } else {
        groupedItems.set(key, {
          ...item,
          quantity: (item.totalQuantity ?? ((item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0))),
          totalPrice: item.unitPrice * (item.totalQuantity ?? ((item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0))),
          orderItemIds: [item.id], // Khởi tạo mảng với ID đầu tiên
          orderItemDetails: [{
            id: item.id,
            pending: item.pendingQuantity || 0,
            preparing: item.preparingQuantity || 0,
            completed: item.completedQuantity || 0
          }] // Lưu chi tiết theo từng cột
        })
      }
    })
    
    // Chuyển về array và sắp xếp theo thời gian order
    const sortedItems = Array.from(groupedItems.values()).sort((a, b) => {
      if (!a.orderTime && !b.orderTime) return 0
      if (!a.orderTime) return 1
      if (!b.orderTime) return -1
      return new Date(a.orderTime).getTime() - new Date(b.orderTime).getTime()
    })
    
    // Bảo toàn các giá trị đã chọn từ data cũ
    const oldItems = pendingOrderItems.value
    const updatedItems = sortedItems.map(newItem => {
      // Tìm item cũ có cùng key để bảo toàn giá trị đã chọn
      const oldItem = oldItems.find(old => 
        `${old.menuItemName}_${(old as any).orderId || old.tableName}_${old.currentStatus}` === 
        `${newItem.menuItemName}_${(newItem as any).orderId || newItem.tableName}_${newItem.currentStatus}`
      )
      
      if (oldItem) {
        return {
          ...newItem,
          newStatus: oldItem.newStatus || newItem.currentStatus,
          updateQuantity: oldItem.updateQuantity !== undefined ? oldItem.updateQuantity : null,
          updating: oldItem.updating || false
        }
      }
      
      return newItem
    })
    
    pendingOrderItems.value = updatedItems
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(`Lỗi tải danh sách món: ${error.message || 'Không thể tải'}`)
  }
}

const selectStatus = (status: OrderItemStatusResponse) => {
  selectedStatus.value = status
}

const getAvailableForStatus = (detail: any, targetStatus: string) => {
  switch (targetStatus) {
    case 'SERVED': return detail.completed || 0
    case 'COMPLETED': return detail.preparing || 0 // chỉ từ PREPARING
    case 'PREPARING': return detail.pending || 0 // từ PENDING → PREPARING
    case 'PENDING': return detail.preparing || 0 // từ PREPARING → PENDING
    case 'CANCELLED':
    case 'OUT_OF_STOCK':
      return detail.pending || 0 // chỉ từ PENDING
    default:
      return 0
  }
}

const getGroupAvailable = (groupItem: any, targetStatus: string) => {
  return (groupItem.orderItemDetails || []).reduce((sum: number, d: any) => sum + getAvailableForStatus(d, targetStatus), 0)
}

// Helpers: upsert/merge realtime payloads into local list (no refetch)
const recomputeGroupTotals = (group: any) => {
  const details = group.orderItemDetails || []
  const sums = details.reduce((acc: any, d: any) => {
    acc.pending += Number(d.pending || 0)
    acc.preparing += Number(d.preparing || 0)
    acc.completed += Number(d.completed || 0)
    return acc
  }, { pending: 0, preparing: 0, completed: 0 })
  group.pendingQuantity = sums.pending
  group.preparingQuantity = sums.preparing
  group.completedQuantity = sums.completed
  group.quantity = (sums.pending + sums.preparing + sums.completed)
  group.totalPrice = Number(group.unitPrice || 0) * Number(group.quantity || 0)
}

const upsertFromItemPayload = (p: any) => {
  if (!p) return
  const status = (deriveOrderItemStatus(p) || '').toUpperCase()
  const hasManageable = (Number(p.pendingQuantity || 0) > 0) || (Number(p.preparingQuantity || 0) > 0)
  const key = getGroupKey(p)
  const idx = pendingOrderItems.value.findIndex((g: any) => getGroupKey(g) === key)

  // If no longer manageable and group exists, remove detail or whole group
  if (!hasManageable) {
    if (idx >= 0) {
      const group = pendingOrderItems.value[idx]
      group.orderItemDetails = (group.orderItemDetails || []).filter((d: any) => d.id !== p.id)
      group.orderItemIds = (group.orderItemIds || []).filter((id: number) => id !== p.id)
      recomputeGroupTotals(group)
      // remove group if empty
      const pend = Number(group.pendingQuantity || 0)
      const prep = Number(group.preparingQuantity || 0)
      if ((group.orderItemDetails || []).length === 0 || (pend + prep) === 0) {
        pendingOrderItems.value.splice(idx, 1)
      }
    }
    return
  }

  // Upsert group
  if (idx === -1) {
    const newGroup: any = {
      id: p.id, // representative
      menuItemId: p.menuItemId,
      menuItemName: p.menuItemName,
      tableName: p.tableName || 'N/A',
      orderId: p.orderId,
      unitPrice: Number(p.unitPrice || 0),
      totalPrice: 0,
      orderTime: p.orderTime || null,
      orderItemIds: [p.id],
      orderItemDetails: [{ id: p.id, pending: p.pendingQuantity || 0, preparing: p.preparingQuantity || 0, completed: p.completedQuantity || 0 }],
      updating: false,
      newStatus: deriveOrderItemStatus(p),
      currentStatus: deriveOrderItemStatus(p)
    }
    recomputeGroupTotals(newGroup)
    pendingOrderItems.value.push(newGroup)
  } else {
    const group = pendingOrderItems.value[idx]
    // ensure arrays
    group.orderItemIds = group.orderItemIds || []
    group.orderItemDetails = group.orderItemDetails || []
    const dIdx = group.orderItemDetails.findIndex((d: any) => d.id === p.id)
    if (dIdx === -1) {
      group.orderItemIds.push(p.id)
      group.orderItemDetails.push({ id: p.id, pending: p.pendingQuantity || 0, preparing: p.preparingQuantity || 0, completed: p.completedQuantity || 0 })
    } else {
      const d = group.orderItemDetails[dIdx]
      d.pending = p.pendingQuantity || 0
      d.preparing = p.preparingQuantity || 0
      d.completed = p.completedQuantity || 0
    }
    recomputeGroupTotals(group)
  }
}

const upsertFromItemsPayload = (items: any[]) => {
  if (!Array.isArray(items)) return
  items.forEach(upsertFromItemPayload)
}

// Optimistic update functions - cập nhật local state ngay lập tức
const updateItemStatusOptimistic = (item: any, newStatus: string, quantity: number) => {
  // Không set thẳng currentStatus theo newStatus vì có thể chỉ cập nhật một phần
  // Luôn tính lại từ chi tiết để suy ra trạng thái nhóm
  
  // Cập nhật chi tiết per-status theo remaining để không vượt quá số lượng yêu cầu
  if (item.orderItemDetails) {
    let remaining = quantity
    // Sắp xếp chi tiết theo lượng khả dụng giảm dần để tiêu thụ hợp lý
    const detailsSorted = [...item.orderItemDetails].sort((a: any, b: any) => {
      return getAvailableForStatus(b, newStatus) - getAvailableForStatus(a, newStatus)
    })
    for (const detail of detailsSorted) {
      if (remaining <= 0) break
      const available = getAvailableForStatus(detail, newStatus)
      const takeAmt = Math.min(remaining, available)
      if (takeAmt <= 0) continue
      switch (newStatus) {
        case 'SERVED': {
          detail.completed = Math.max(0, (detail.completed || 0) - takeAmt)
          ;(detail as any).served = ((detail as any).served || 0) + takeAmt
          break
        }
        case 'COMPLETED': {
          // chỉ lấy từ PREPARING
          const fromPreparing = Math.min(takeAmt, detail.preparing || 0)
          detail.preparing = Math.max(0, (detail.preparing || 0) - fromPreparing)
          detail.completed = (detail.completed || 0) + fromPreparing
          break
        }
        case 'PREPARING': {
          const fromPending = Math.min(takeAmt, detail.pending || 0)
          detail.pending = Math.max(0, (detail.pending || 0) - fromPending)
          detail.preparing = (detail.preparing || 0) + fromPending
          break
        }
        case 'PENDING': {
          const fromPreparing = Math.min(takeAmt, detail.preparing || 0)
          detail.preparing = Math.max(0, (detail.preparing || 0) - fromPreparing)
          detail.pending = (detail.pending || 0) + fromPreparing
          break
        }
        case 'CANCELLED':
        case 'OUT_OF_STOCK': {
          const fromPending = Math.min(takeAmt, detail.pending || 0)
          detail.pending = Math.max(0, (detail.pending || 0) - fromPending)
          if (newStatus === 'CANCELLED') {
            ;(detail as any).cancelled = ((detail as any).cancelled || 0) + fromPending
          } else {
            ;(detail as any).outOfStock = ((detail as any).outOfStock || 0) + fromPending
          }
          break
        }
      }
      remaining -= takeAmt
    }
  }
  
  // Tính lại tổng từ chi tiết
  const sums = (item.orderItemDetails || []).reduce((acc: any, d: any) => {
    acc.pending += Number(d.pending || 0)
    acc.preparing += Number(d.preparing || 0)
    acc.completed += Number(d.completed || 0)
    return acc
  }, { pending: 0, preparing: 0, completed: 0 })
  item.pendingQuantity = sums.pending
  item.preparingQuantity = sums.preparing
  item.completedQuantity = sums.completed
  item.quantity = sums.pending + sums.preparing + sums.completed

  // Suy ra trạng thái hiện tại của nhóm sau khi cập nhật chi tiết
  if (item.quantity > 0) {
    if (sums.pending > 0) {
      item.currentStatus = 'PENDING'
    } else if (sums.preparing > 0) {
      item.currentStatus = 'PREPARING'
    } else if (sums.completed > 0) {
      item.currentStatus = 'COMPLETED'
    } else {
      item.currentStatus = 'SERVED'
    }
  }

  // Nếu item đã hết quantity, xóa khỏi danh sách
  if (item.quantity <= 0) {
    const index = pendingOrderItems.value.findIndex(i => getGroupKey(i) === getGroupKey(item))
    if (index > -1) {
      pendingOrderItems.value.splice(index, 1)
    }
  }
}

const updateItemStatus = async (item: any) => {
  if (!item.newStatus) return
  
  try {
    item.updating = true
    
    // Xử lý cập nhật theo số lượng cụ thể
    const updateQuantityNum = Number(item.updateQuantity)
    const maxAvailable = getGroupAvailable(item, item.newStatus)
    
    // Nếu có chọn số lượng -> luôn xử lý theo số lượng (không dùng nhánh cập nhật toàn bộ)
    if (updateQuantityNum && updateQuantityNum > 0) {
      const qtyToApply = Math.min(updateQuantityNum, maxAvailable)
      if (qtyToApply <= 0) {
        item.updating = false
        return
      }
      // TÍNH KẾ HOẠCH TRƯỚC KHI OPTIMISTIC (tránh làm giảm available rồi không gọi API)
      let remainingPlan = qtyToApply
      const sortedBefore = [...(item.orderItemDetails || [])].sort((a: any, b: any) => getAvailableForStatus(b, item.newStatus) - getAvailableForStatus(a, item.newStatus))
      const plan: Array<{id: number, qty: number}> = []
      for (let i = 0; i < sortedBefore.length && remainingPlan > 0; i++) {
        const d = sortedBefore[i]
        const avail = getAvailableForStatus(d, item.newStatus)
        const take = Math.min(avail, remainingPlan)
        if (take > 0) {
          plan.push({ id: d.id, qty: take })
          remainingPlan -= take
        }
      }

      // OPTIMISTIC UPDATE: Cập nhật UI ngay lập tức
      updateItemStatusOptimistic(item, item.newStatus, qtyToApply)

      // Gọi API theo kế hoạch đã tính từ dữ liệu trước khi optimistic
      for (const step of plan) {
        await OrderItemApi.updateStatusPartial(step.id, item.newStatus, step.qty)
      }
      
      const toast = useNuxtApp().$toast as typeof toastType
      toast.success(`Cập nhật ${qtyToApply} phần "${item.menuItemName}" thành công!`)
      
      // Reset dropdown số lượng
      item.updateQuantity = null
    } else {
      // OPTIMISTIC UPDATE: Cập nhật UI ngay lập tức
      const totalAvailable = getGroupAvailable(item, item.newStatus)
      updateItemStatusOptimistic(item, item.newStatus, totalAvailable)
      
      // Gọi API để cập nhật server
      await OrderItemApi.updateMultipleStatusesQuick(item.orderItemIds, item.newStatus)
      
      const toast = useNuxtApp().$toast as typeof toastType
      
      // Nếu cập nhật thành COMPLETED, thông báo cho nhân viên phục vụ
      if (item.newStatus === 'COMPLETED') {
        toast.success(`Món "${item.menuItemName}" đã hoàn tất! Thông báo cho nhân viên phục vụ.`)
      } else {
        toast.success(`Cập nhật trạng thái "${item.menuItemName}" thành công!`)
      }
    }
  } catch (error: any) {
    // Revert optimistic update on error
    await fetchPendingOrderItems()
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(`Lỗi cập nhật trạng thái: ${error.message || 'Không thể cập nhật'}`)
  } finally {
    item.updating = false
  }
}

const refreshStatuses = async () => {
  await Promise.all([
    fetchOrderItemStatuses(),
    fetchPendingOrderItems()
  ])
}

// Tạo options cho dropdown số lượng
const getQuantityOptions = (totalQuantity: number) => {
  const options: Array<{ label: string, value: number | null }> = [
    { label: 'Tất cả', value: null }
  ]
  
  // Thêm các option từ 1 đến totalQuantity
  for (let i = 1; i <= totalQuantity; i++) {
    options.push({
      label: `${i} phần`,
      value: i
    })
  }
  
  return options
}



const getItemStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'PENDING': 'gray',
    'PREPARING': 'yellow',
    'COMPLETED': 'green',
    'SERVED': 'blue',
    'CANCELLED': 'red',
    'OUT_OF_STOCK': 'orange',
    'pending': 'gray',
    'preparing': 'yellow',
    'ready': 'green',
    'served': 'blue',
    'Đang chế biến': 'yellow',
    'Hoàn tất': 'green',
    'Đã phục vụ': 'blue',
    'Chờ xử lý': 'gray'
  }
  return colors[status] || 'gray'
}

const getStatusDisplayName = (status: string) => {
  const displayNames: Record<string, string> = {
    'PENDING': 'Chờ xử lý',
    'PREPARING': 'Đang chế biến',
    'COMPLETED': 'Hoàn tất',
    'SERVED': 'Đã phục vụ',
    'CANCELLED': 'Đã hủy',
    'OUT_OF_STOCK': 'Hết món',
    'pending': 'Chờ xử lý',
    'preparing': 'Đang chế biến',
    'ready': 'Hoàn tất',
    'served': 'Đã phục vụ',
    'Đang chế biến': 'Đang chế biến',
    'Hoàn tất': 'Hoàn tất',
    'Đã phục vụ': 'Đã phục vụ',
    'Chờ xử lý': 'Chờ xử lý'
  }
  return displayNames[status] || status
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatOrderTime = (orderTime?: string) => {
  if (!orderTime) return ''
  try {
    const date = new Date(orderTime)
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return ''
  }
}

// Lifecycle
onMounted(async () => {
  await fetchOrderItemStatuses()
  await fetchPendingOrderItems()

  // Subscribe realtime cho toàn bộ danh sách món cần quản lý
  const nuxt = useNuxtApp() as any
  if (nuxt?.$realtime) {
    nuxt.$realtime.subscribe('/topic/management/order-items', (msg: any) => {
      if (!msg) return
      if (msg.type === 'ORDER_ITEM_STATUS_CHANGED' && msg.item) {
        upsertFromItemPayload(msg.item)
        // Fallback đồng bộ nếu payload chưa đầy đủ hoặc state chưa khớp
        setTimeout(() => { fetchPendingOrderItems() }, 0)
      } else if (msg.type === 'ORDERED_UPDATED' && Array.isArray(msg.items)) {
        upsertFromItemsPayload(msg.items)
        setTimeout(() => { fetchPendingOrderItems() }, 0)
      }
    })
  }

  // Subscribe realtime theo order nếu có
  const orderId = (props as any).orderId
  if (orderId && nuxt?.$realtime) {
    nuxt.$realtime.subscribe(`/topic/orders/${orderId}`, (msg: any) => {
      // Tìm item có chứa orderItemId trong mảng orderItemIds
      const idx = pendingOrderItems.value.findIndex(i => 
        i.orderItemIds && i.orderItemIds.includes(msg.orderItemId)
      )
      if (idx >= 0) {
        const item = pendingOrderItems.value[idx]
        
        // Sử dụng optimistic update thay vì refresh toàn bộ
        if (msg.status === 'COMPLETED' || msg.status === 'SERVED') {
          // Cập nhật trạng thái item ngay lập tức
          item.currentStatus = msg.status
          // Nếu tất cả order items trong nhóm đã hoàn tất, xóa khỏi danh sách
          if ((item as any).quantity <= 0) {
            pendingOrderItems.value.splice(idx, 1)
          }
        }
      }
    })
  }

  // Không dùng polling nữa; mọi cập nhật đến từ realtime + optimistic
})

// Expose methods
defineExpose({
  refreshStatuses,
  fetchPendingOrderItems
})
</script>
