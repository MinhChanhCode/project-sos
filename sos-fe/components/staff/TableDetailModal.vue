<template>
  <UModal v-model="isOpen" :ui="{ width: 'max-w-3xl' }">
    <UCard v-if="table">
      <template #header>
        <h2 class="text-lg font-semibold">Chi tiết {{ table.number }}</h2>
      </template>

      <div class="space-y-4 max-h-96 overflow-y-auto">
        <div class="flex justify-between items-center">
          <div>
            <UBadge :color="getStatusColor(table.status)" variant="soft">
              {{ getStatusText(table.status) }}
            </UBadge>
            <p class="text-sm text-gray-600 mt-1">
              {{ table.customers }} khách • {{ table.assignedStaff }}
            </p>
            <p class="text-sm text-gray-500 mt-1">
              Tổng: {{ allItems?.length || 0 }} món
            </p>
          </div>
          <div class="text-right">
            <div class="font-bold text-lg">{{ formatPrice(table.totalAmount) }}</div>
          </div>
        </div>

        <!-- Tất cả món của bàn (đã gộp theo tên và trạng thái) -->
        <div v-if="groupedItems && groupedItems.length > 0">
          <div class="space-y-3">
            <div 
              v-for="(group, groupIndex) in groupedItems" 
              :key="`${group.name}_${group.status}`" 
              class="flex items-center justify-between p-3 border rounded-lg"
            >
              <div class="flex-1">
                <div class="flex items-center space-x-2">
                  <h4 class="font-medium">{{ group.name }}</h4>
                  <Icon v-if="group.needsKitchen" name="lucide:chef-hat" class="w-4 h-4 text-orange-500" />
                </div>
                <p class="text-sm text-gray-600">
                  Số lượng: {{ group.quantity }} • {{ formatPrice(group.price) }}
                  <span v-if="group.orderTime" class="ml-2 text-blue-600 font-medium">
                    • 🕐 {{ formatOrderTime(group.orderTime) }}
                  </span>
                </p>
                <p v-if="group.notes" class="text-sm text-blue-600">Ghi chú: {{ group.notes }}</p>
              </div>
              <div class="flex flex-col space-y-2">
                <!-- Hiển thị tất cả trạng thái của món -->
                <div class="flex flex-wrap gap-2 text-sm">
                  <span v-if="getStatusCount(group, 'pending') > 0" class="px-2 py-1 bg-gray-100 rounded">
                    Chờ xử lý: {{ getStatusCount(group, 'pending') }}
                  </span>
                  <span v-if="getStatusCount(group, 'preparing') > 0" class="px-2 py-1 bg-yellow-100 rounded">
                    Đang chế biến: {{ getStatusCount(group, 'preparing') }}
                  </span>
                  <span v-if="getStatusCount(group, 'completed') > 0" class="px-2 py-1 bg-green-100 rounded">
                    Hoàn tất: {{ getStatusCount(group, 'completed') }}
                  </span>
                  <span v-if="getStatusCount(group, 'served') > 0" class="px-2 py-1 bg-blue-100 rounded">
                    Đã phục vụ: {{ getStatusCount(group, 'served') }}
                  </span>
                </div>
                
                <!-- Chỉ hiển thị button "Đã phục vụ" cho món đã hoàn tất -->
                <div class="flex space-x-2">
                  <!-- Button cho món completed - đã phục vụ -->
                  <UButton 
                    v-if="getStatusCount(group, 'completed') > 0"
                    @click="handleUpdateGroupStatus(group, 'served')"
                    size="sm"
                    color="blue"
                  >
                    Đã phục vụ ({{ getStatusCount(group, 'completed') }})
                  </UButton>
                  
                  <!-- Hiển thị trạng thái đã được phục vụ cho món đã phục vụ -->
                  <UButton 
                    v-if="getStatusCount(group, 'served') > 0"
                    disabled
                    variant="outline"
                    size="sm"
                  >
                    <Icon name="lucide:check-circle" class="w-4 h-4 mr-1" />
                    Đã phục vụ ({{ getStatusCount(group, 'served') }})
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex flex-wrap gap-2">
          <UButton variant="outline" class="flex-1" @click="handlePayment" :loading="paying">
            <Icon name="lucide:credit-card" class="w-4 h-4 mr-2" />
            Thanh toán
          </UButton>
          <UButton variant="outline" class="flex-1" @click="$emit('chat-customer')">
            <Icon name="lucide:message-circle" class="w-4 h-4 mr-2" />
            Chat
          </UButton>
          <UButton
            class="flex-1"
            :disabled="!canClearTable"
            :color="canClearTable ? 'green' : 'gray'"
            @click="handleClearTable"
          >
            <Icon name="lucide:broom" class="w-4 h-4 mr-2" />
            {{ canClearTable ? 'Dọn bàn' : 'Chưa thể dọn bàn' }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>

  <UModal v-model="showPaymentBill" :ui="{ width: 'max-w-2xl' }">
    <UCard v-if="currentInvoice">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-lg font-semibold">{{ currentInvoice.restaurantName || "Gọi Món Bistro" }}</h2>
            <p class="text-sm text-gray-500">{{ currentInvoice.invoiceCode || ("INV-" + currentInvoice.orderId) }}</p>
          </div>
          <UBadge :color="currentInvoice.status === 'PAID' ? 'green' : 'yellow'" variant="soft">
            {{ currentInvoice.status === 'PAID' ? 'Đã thanh toán' : 'Chờ thanh toán' }}
          </UBadge>
        </div>
      </template>

      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3 text-sm">
          <div>
            <div class="text-gray-500">Bàn</div>
            <div class="font-medium">{{ currentInvoice.tableName || table?.number }}</div>
          </div>
          <div>
            <div class="text-gray-500">Khách hàng</div>
            <div class="font-medium">{{ currentInvoice.customerName || "Khách tại bàn" }}</div>
          </div>
          <div>
            <div class="text-gray-500">Order</div>
            <div class="font-medium">#{{ currentInvoice.orderId }}</div>
          </div>
          <div>
            <div class="text-gray-500">Thời gian</div>
            <div class="font-medium">{{ formatInvoiceDate(currentInvoice.createdAt) }}</div>
          </div>
        </div>

        <div class="rounded-lg border">
          <div class="grid grid-cols-[1fr_56px_96px_104px] gap-2 border-b px-3 py-2 text-xs font-semibold text-gray-500">
            <span>Món</span>
            <span>SL</span>
            <span>Đơn giá</span>
            <span>Thành tiền</span>
          </div>
          <div
            v-for="item in currentInvoice.items || []"
            :key="item.orderItemId"
            class="grid grid-cols-[1fr_56px_96px_104px] gap-2 px-3 py-2 text-sm"
          >
            <span>{{ item.menuItemName }}</span>
            <span>{{ item.quantity }}</span>
            <span>{{ formatPrice(Number(item.unitPrice || 0)) }}</span>
            <span>{{ formatPrice(Number(item.lineTotal || 0)) }}</span>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-[1fr_180px]">
          <div class="space-y-2 text-sm">
            <div class="flex justify-between"><span>Tạm tính</span><b>{{ formatPrice(Number(currentInvoice.subtotal || 0)) }}</b></div>
            <div v-if="Number(currentInvoice.discount || 0) > 0" class="flex justify-between"><span>Giảm giá</span><b>{{ formatPrice(Number(currentInvoice.discount || 0)) }}</b></div>
            <div v-if="Number(currentInvoice.serviceFee || 0) > 0" class="flex justify-between"><span>Phí dịch vụ</span><b>{{ formatPrice(Number(currentInvoice.serviceFee || 0)) }}</b></div>
            <div class="flex justify-between border-t pt-2 text-base"><span>Tổng tiền</span><b>{{ formatPrice(Number(currentInvoice.total || 0)) }}</b></div>
          </div>
          <div class="flex flex-col items-center rounded-lg border p-3">
            <qrcode-vue :value="currentInvoice.paymentQrPayload || ''" :size="144" level="M" render-as="svg" />
            <div class="mt-2 text-center text-xs text-gray-500">QR thanh toán demo</div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex gap-2">
          <UButton variant="ghost" class="flex-1" @click="showPaymentBill = false">Đóng</UButton>
          <UButton color="green" class="flex-1" :loading="paying" @click="confirmPayment">
            Xác nhận đã thanh toán
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { useNuxtApp } from 'nuxt/app'
import QrcodeVue from 'qrcode.vue'
import { TableApi, OrderItemApi } from '@/api-service'
import { invoiceApi } from '@/api-service/ExtendedApi'
import type { toast as toastType } from 'vue3-toastify'
import { getStatusColor, getStatusText, formatPrice, deriveOrderItemStatus } from '~/utils/formatters'
import { useStaffStore } from '@/stores/staff'
import { useAuthStore } from '@/stores/auth'

interface OrderItem {
  id: string
  name: string
  quantity: number
  price: number
  status: string
  needsKitchen: boolean
  notes?: string
  orderTime: string
  orderId: string
  orderIndex: number
  itemIndex: number
}

interface Order {
  id: string
  items: OrderItem[]
  status: string
  orderTime: string
  orderNumber?: number // Added orderNumber
}

interface Table {
  id: string
  number: string
  status: "trống" | "đang đặt" | "chờ phục vụ" | "đang ăn" | "thanh toán" | "đã phục vụ" | "sẵn sàng" | "đang chế biến" | "có khách"
  customers: number
  assignedStaff: string
  totalAmount: number
  orders: Order[]
}

interface Props {
  modelValue: boolean
  table: Table | null
}

const props = defineProps<Props>()

// Proxy v-model for modal
const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

// Access latest table state from store to avoid stale prop references
const staffStore = useStaffStore()
const authStore = useAuthStore()
const sourceTable = computed(() => {
  if (!props.table?.id) return props.table
  return staffStore.getTableById(String(props.table.id)) || props.table
})

// Reactive state để quản lý trạng thái items
const localOrders = ref<Order[]>([])
const paying = ref(false)
const activeOrderId = ref<number | null>(null)
const showPaymentBill = ref(false)
const currentInvoice = ref<any | null>(null)
const subscribedTableIds = new Set<string>()

// Load latest table detail and split quantities into per-status items
const loadTableOrders = async () => {
  if (!props.table?.id) return
  try {
    const detail = await TableApi.getDetail(String(props.table.id))
    const d: any = (detail as any)?.data ?? detail
    activeOrderId.value = d?.activeOrderId ?? null
    const allItems: any[] = []

    if (d?.sessionItems?.length) {
      d.sessionItems.forEach((item: any) => {
        const q = {
          pending: Number(item.pendingQuantity || 0),
          preparing: Number(item.preparingQuantity || 0),
          completed: Number(item.completedQuantity || 0),
          served: Number(item.servedQuantity || 0)
        }

        if (q.pending > 0) allItems.push({ id: `${item.id}_pending`, name: item.menuItemName, quantity: q.pending, price: Number(item.unitPrice), status: 'pending', needsKitchen: true, notes: item.notes || '', orderTime: item.orderTime || 'Giỏ hàng hiện tại', orderId: item.orderId, orderIndex: 0, itemIndex: 0 })
        if (q.preparing > 0) allItems.push({ id: `${item.id}_preparing`, name: item.menuItemName, quantity: q.preparing, price: Number(item.unitPrice), status: 'preparing', needsKitchen: true, notes: item.notes || '', orderTime: item.orderTime || 'Giỏ hàng hiện tại', orderId: item.orderId, orderIndex: 0, itemIndex: 0 })
        if (q.completed > 0) allItems.push({ id: `${item.id}_completed`, name: item.menuItemName, quantity: q.completed, price: Number(item.unitPrice), status: 'completed', needsKitchen: true, notes: item.notes || '', orderTime: item.orderTime || 'Giỏ hàng hiện tại', orderId: item.orderId, orderIndex: 0, itemIndex: 0 })
        if (q.served > 0) allItems.push({ id: `${item.id}_served`, name: item.menuItemName, quantity: q.served, price: Number(item.unitPrice), status: 'served', needsKitchen: true, notes: item.notes || '', orderTime: item.orderTime || 'Giỏ hàng hiện tại', orderId: item.orderId, orderIndex: 0, itemIndex: 0 })
      })
    }

    // sort by orderTime
    const sorted = allItems.sort((a: any, b: any) => {
      if (a.orderTime === 'Giỏ hàng hiện tại' && b.orderTime !== 'Giỏ hàng hiện tại') return -1
      if (b.orderTime === 'Giỏ hàng hiện tại' && a.orderTime !== 'Giỏ hàng hiện tại') return 1
      try { return new Date(a.orderTime).getTime() - new Date(b.orderTime).getTime() } catch { return 0 }
    })

    localOrders.value = [{ id: 'all-items', items: sorted, status: 'active', orderTime: 'Tất cả món' } as any]
  } catch (error) {
    console.error('Error loading table orders:', error)
  }
}

// Load when open or table changes
watch(() => props.modelValue, (open) => { if (open) loadTableOrders() }, { immediate: true })
watch(() => props.table?.id, () => { if (props.modelValue) loadTableOrders() })

// Optimistic update functions
const addItemOptimistic = (item: any) => {
  // Thêm item mới vào localOrders ngay lập tức
  if (localOrders.value.length === 0) {
    localOrders.value = [{ id: 'all-items', items: [], status: 'active', orderTime: 'Tất cả món' }]
  }
  
  const newItem = {
    id: `${item.id}_pending`,
    name: item.menuItemName,
    quantity: item.quantity || 1,
    price: Number(item.unitPrice),
    status: 'pending',
    needsKitchen: true,
    notes: item.notes || '',
    orderTime: 'Giỏ hàng hiện tại',
    orderId: item.orderId,
    orderIndex: 0,
    itemIndex: 0
  }
  
  localOrders.value[0].items.push(newItem)
  
  // Sync lại từ server sau delay
  setTimeout(() => {
    loadTableOrders()
  }, 1000)
}

const updateItemStatusOptimistic = (itemId: string, newStatus: string) => {
  // Cập nhật trạng thái item trong localOrders ngay lập tức
  localOrders.value.forEach(order => {
    order.items.forEach(item => {
      if (item.id === itemId) {
        item.status = newStatus.toLowerCase()
      }
    })
  })
  
  // Sync lại từ server sau delay
  setTimeout(() => {
    loadTableOrders()
  }, 500)
}

// Real-time subscription for table updates
watch(() => [props.table?.id, props.modelValue], ([tableId, isOpen]) => {
  if (!tableId || !isOpen) return
  const subscriptionKey = String(tableId)
  if (subscribedTableIds.has(subscriptionKey)) return
  subscribedTableIds.add(subscriptionKey)
  
  const nuxt = useNuxtApp() as any
  if (nuxt?.$realtime) {
    
    // Subscribe to table-specific updates
    nuxt.$realtime.subscribe(`/topic/tables/${tableId}/order-items`, (msg: any) => {
      if (msg?.type === 'ORDER_ITEM_STATUS_CHANGED') {
        // Sử dụng optimistic update thay vì refresh toàn bộ
        if (msg.orderItemId) {
          updateItemStatusOptimistic(`${msg.orderItemId}_completed`, 'served')
        } else {
          loadTableOrders() // Fallback: refresh toàn bộ
        }
      }
    })
    
    // Subscribe to general table updates
    nuxt.$realtime.subscribe(`/topic/tables/${tableId}`, (msg: any) => {
      if (msg?.type === 'ORDER_STATUS_UPDATED' || msg?.type === 'ORDERED_UPDATED') {
        loadTableOrders() // Refresh data when table status changes
      } else if (msg?.type === 'TABLE_CLEARED') {
        // Clear local immediately when table is cleared elsewhere
        localOrders.value = []
        try {
          const tbl = sourceTable.value as any
          if (tbl) {
            tbl.orders = []
            tbl.totalAmount = 0
          }
        } catch {}
      }
    })
    
    // Subscribe to new orders/items added
    nuxt.$realtime.subscribe(`/topic/tables/${tableId}/ordered`, (msg: any) => {
      if (msg?.type === 'ORDERED_UPDATED') {
        loadTableOrders() // Refresh data when new items are added
      }
    })
  }
}, { immediate: true })

// Gộp tất cả món từ các orders và sắp xếp theo thời gian
const allItems = computed(() => {
  if (!localOrders.value || localOrders.value.length === 0) {
    return []
  }
  
  const items: (OrderItem & { orderId: string; orderIndex: number; orderTime: string })[] = []
  
  localOrders.value.forEach((order, orderIndex) => {
    order.items.forEach(item => {
      items.push({
        ...item,
        orderId: order.id,
        orderIndex,
        orderTime: item.orderTime || order.orderTime // Ưu tiên item.orderTime, fallback về order.orderTime
      })
    })
  })
  
  // Sắp xếp theo thời gian order (món order trước hiển thị trên)
  return items.sort((a, b) => {
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
})

// Gộp các món giống nhau theo tên và trạng thái
const groupedItems = computed(() => {
  if (!allItems.value || allItems.value.length === 0) {
    return []
  }
  
  const grouped = new Map<string, {
    name: string
    quantity: number
    price: number
    status: string
    needsKitchen: boolean
    notes?: string
    orderTime: string
    items: Array<{
      id: string
      orderId: string
      orderIndex: number
      itemIndex: number
      quantity: number
      status: string
    }>
  }>()
  
  allItems.value.forEach((item, itemIndex) => {
    // Group by item name only, not by status
    const itemStatus = item.status || 'pending'
    const key = item.name
    
    if (grouped.has(key)) {
      const existing = grouped.get(key)!
      // Không cộng dồn quantity, để getStatusCount tính đúng
      existing.items.push({
        id: item.id,
        orderId: item.orderId,
        orderIndex: item.orderIndex,
        itemIndex,
        quantity: item.quantity,
        status: itemStatus
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
      grouped.set(key, {
        name: item.name,
        quantity: item.quantity,
        price: item.price,
        status: 'mixed', // Mixed status when items have different statuses
        needsKitchen: item.needsKitchen,
        notes: item.notes,
        orderTime: item.orderTime,
        items: [{
          id: item.id,
          orderId: item.orderId,
          orderIndex: item.orderIndex,
          itemIndex,
          quantity: item.quantity,
          status: itemStatus
        }]
      })
    }
  })
  
  const result = Array.from(grouped.values())
  
  // Tính lại quantity total cho mỗi group
  result.forEach(group => {
    group.quantity = group.items.reduce((sum, item) => sum + item.quantity, 0)
  })
  
  return result.sort((a, b) => {
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
})

// Kiểm tra xem có thể dọn bàn không (chỉ khi TẤT CẢ món đã sẵn sàng phục vụ)
const canClearTable = computed(() => {
  if (!groupedItems.value || groupedItems.value.length === 0) {
    return true // Bàn trống, có thể dọn
  }
  // Chỉ cho phép dọn bàn khi TẤT CẢ món đã served (không còn pending/preparing/completed)
  return groupedItems.value.every(group => {
    const pendingCount = getStatusCount(group, 'pending')
    const preparingCount = getStatusCount(group, 'preparing')
    const completedCount = getStatusCount(group, 'completed')
    return pendingCount === 0 && preparingCount === 0 && completedCount === 0
  })
})

const canClearByRole = computed(() => {
  authStore.loadFromStorage()
  return authStore.hasAnyRole(['ADMIN', 'MANAGER', 'STAFF', 'KITCHEN'])
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update-item-status': [tableId: string, orderId: string, itemId: string, newStatus: string]
  'chat-customer': []
}>()

// Function để cập nhật trạng thái item ngay lập tức
const updateItemStatus = (orderIndex: number, itemIndex: number, newStatus: 'pending' | 'preparing' | 'ready' | 'served') => {
  if (localOrders.value[orderIndex] && localOrders.value[orderIndex].items[itemIndex]) {
    localOrders.value[orderIndex].items[itemIndex].status = newStatus
  }
}

const handleUpdateItemStatus = (tableId: string, orderId: string, itemId: string, newStatus: 'pending' | 'preparing' | 'ready' | 'served' | 'completed', orderIndex: number, itemIndex: number) => {
  // Cập nhật trạng thái trong localOrders trước
  if (localOrders.value[orderIndex] && localOrders.value[orderIndex].items[itemIndex]) {
    localOrders.value[orderIndex].items[itemIndex].status = newStatus
  }
  
  // Force re-compute allItems để cập nhật UI ngay lập tức
  nextTick(() => {
    // allItems sẽ tự động cập nhật vì nó là computed từ localOrders
  })
  
  emit('update-item-status', tableId, orderId, itemId, newStatus)
}

// Function để đếm số lượng items theo status
const getStatusCount = (group: any, status: string) => {
  return group.items
    .filter((item: any) => item.status === status)
    .reduce((sum: number, item: any) => sum + (Number(item.quantity) || 0), 0)
}

const handleUpdateGroupStatus = async (group: any, newStatus: 'served') => {
  // Chỉ cập nhật các items có status 'completed' sang 'served'
  const completedItems = group.items.filter((item: any) => item.status === 'completed')
  
  try {
    // OPTIMISTIC UPDATE: Cập nhật UI ngay lập tức
    const originalItemId = completedItems[0]?.id.replace('_completed', '')
    const numericId = Number(originalItemId)
    
    if (!originalItemId || originalItemId === '0' || isNaN(numericId) || numericId <= 0) {
      throw new Error(`Invalid item ID: ${completedItems[0]?.id}`)
    }
    
    // Cập nhật local state ngay lập tức
    const groupIndex = groupedItems.value.findIndex(g => g.name === group.name)
    if (groupIndex >= 0) {
      const currentGroup = groupedItems.value[groupIndex]
      const completedCount = getStatusCount(currentGroup, 'completed')
      const servedCount = getStatusCount(currentGroup, 'served')
      
      // Chuyển completed sang served trong UI
      if (completedCount > 0) {
        // Tìm và cập nhật items trong localOrders
        localOrders.value.forEach(order => {
          order.items.forEach(item => {
            if (item.name === group.name && item.status === 'completed') {
              item.status = 'served'
            }
          })
        })
      }
    }
    
    // Gọi API để cập nhật trạng thái
    for (const item of completedItems) {
      // Extract original item ID from the generated ID (remove _completed suffix)
      const originalItemId = item.id.replace('_completed', '')
      const numericId = Number(originalItemId)
      
      if (!originalItemId || originalItemId === '0' || isNaN(numericId) || numericId <= 0) {
        console.error('Invalid originalItemId:', originalItemId, 'numericId:', numericId)
        throw new Error(`Invalid item ID: ${item.id} -> ${originalItemId} -> ${numericId}`)
      }
      
      await OrderItemApi.updateStatusQuick(numericId, 'SERVED')
    }
    
    // Sync lại từ server sau delay nhỏ để đảm bảo UI đã cập nhật
    setTimeout(async () => {
      await loadTableOrders()
    }, 500)
    
    const toast = useNuxtApp().$toast as typeof toastType
    toast.success(`Đã phục vụ ${completedItems.length} phần "${group.name}"!`)
  } catch (error: any) {
    console.error('Error updating item status:', error)
    // Revert optimistic update on error
    await loadTableOrders()
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(`Lỗi cập nhật trạng thái: ${error.message || 'Không thể cập nhật'}`)
  }
}

const getItemStatusColor = (status: string) => {
  const colors: Record<string, string> = {
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

const getItemStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': 'Chờ xử lý',
    'preparing': 'Đang chế biến',
    'ready': 'Sẵn sàng',
    'served': 'Đã phục vụ',
    'completed': 'Hoàn tất'
  }
  return statusMap[status] || status
}

const formatOrderTime = (time: string) => {
  if (!time || time === 'Giỏ hàng hiện tại') {
    return ''
  }
  
  try {
    const date = new Date(time)
    // Kiểm tra xem date có hợp lệ không
    if (isNaN(date.getTime())) {
      return ''
    }
    
    // Hiển thị thời gian theo format HH:mm
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } catch (error) {
    console.error('Error formatting order time:', error, time)
    return ''
  }
}

const formatInvoiceDate = (time?: string) => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('vi-VN')
  } catch {
    return ''
  }
}

const handlePayment = async () => {
  if (!activeOrderId.value) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error('Không có đơn hàng để thanh toán')
    return
  }
  paying.value = true
  try {
    currentInvoice.value = await invoiceApi.create(activeOrderId.value)
    showPaymentBill.value = true
  } catch (e: any) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(e.message || 'Không thể tạo hóa đơn')
  } finally {
    paying.value = false
  }
}

const confirmPayment = async () => {
  if (!activeOrderId.value || !currentInvoice.value) return
  paying.value = true
  try {
    currentInvoice.value = await invoiceApi.confirmPayment({
      orderId: activeOrderId.value,
      method: 'DEMO_QR',
      amount: Number(currentInvoice.value.total || props.table?.totalAmount || 0),
    })
    const toast = useNuxtApp().$toast as typeof toastType
    toast.success('Đã xác nhận thanh toán')
  } catch (e: any) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(e.message || 'Thanh toán thất bại')
  } finally {
    paying.value = false
  }
}

const handleClearTable = async () => {
  if (!props.table) return

  if (!canClearByRole.value) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error('Bạn không có quyền dọn bàn. Hãy đăng nhập bằng tài khoản nhân viên, quản lý hoặc quản trị.')
    return
  }
  
  // Kiểm tra xem có món nào chưa được phục vụ không (chỉ SERVED mới được coi là đã phục vụ)
  const hasUnservedItems = groupedItems.value.some(group => {
    const pendingCount = getStatusCount(group, 'pending')
    const preparingCount = getStatusCount(group, 'preparing')
    const completedCount = getStatusCount(group, 'completed')
    return pendingCount > 0 || preparingCount > 0 || completedCount > 0
  })
  
  if (hasUnservedItems) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error('Không thể dọn bàn! Vẫn còn món chưa được phục vụ.')
    return
  }
  
  try {
    await TableApi.clear(String(props.table.id))
    const toast = useNuxtApp().$toast as typeof toastType
    toast.success(`Đã dọn ${props.table.number}. Bàn sẵn sàng cho khách mới!`)
      // Reset tạm thời UI của bàn về trạng thái trống
      if (props.table) {
        props.table.totalAmount = 0
        props.table.orders = [] as any
      }
      localOrders.value = []
      // Xóa cache local phía khách nếu có (orderedItems/cart theo session)
      try {
        const url = new URL(window.location.href)
        const sessionId = url.searchParams.get('sessionId')
        if (sessionId) {
          localStorage.removeItem(`orderedItems:${sessionId}`)
          localStorage.removeItem(`cart:${sessionId}`)
        }
      } catch {}
    emit('update:modelValue', false)
  } catch (e: any) {
    const toast = useNuxtApp().$toast as typeof toastType
    toast.error(`Không thể dọn bàn: ${e?.message || 'Lỗi không xác định'}`)
  }
}
</script>
