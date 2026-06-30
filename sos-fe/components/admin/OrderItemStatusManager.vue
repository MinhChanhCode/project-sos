<template>
  <UCard>
    <template #header>
      <h3 class="text-lg font-semibold">Quản lý trạng thái món ăn</h3>
    </template>

    <div class="space-y-6">
      <!-- API Test Section -->
      <div class="space-y-4">
        <h4 class="font-medium text-base">Test các API:</h4>
        
        <!-- Load Pending Items Button -->
        <UButton @click="loadPendingItems" :loading="loadingPending" class="w-full" color="green">
          <Icon name="lucide:list" class="w-4 h-4 mr-2" />
          GET /api/v1/order-items/pending-for-management
        </UButton>

        <!-- Load Items by Order ID -->
        <div class="flex gap-2">
          <UInput 
            v-model="orderId" 
            placeholder="Nhập Order ID" 
            type="number"
            class="flex-1"
          />
          <UButton @click="loadItemsByOrderId" :loading="loadingByOrder" :disabled="!orderId">
            <Icon name="lucide:search" class="w-4 h-4 mr-2" />
            GET /api/v1/order-items/order/{id}
          </UButton>
        </div>

        <!-- Load Item by ID -->
        <div class="flex gap-2">
          <UInput 
            v-model="itemId" 
            placeholder="Nhập Order Item ID" 
            type="number"
            class="flex-1"
          />
          <UButton @click="loadItemById" :loading="loadingById" :disabled="!itemId">
            <Icon name="lucide:search" class="w-4 h-4 mr-2" />
            GET /api/v1/order-items/{id}
          </UButton>
        </div>

        <!-- Update Status Section -->
        <div class="border-t pt-4">
          <h5 class="font-medium mb-3">Cập nhật trạng thái:</h5>
          <div class="flex gap-2">
            <UInput 
              v-model="updateItemId" 
              placeholder="Order Item ID" 
              type="number"
              class="flex-1"
            />
            <USelect 
              v-model="selectedStatus" 
              :options="statusOptions"
              placeholder="Chọn trạng thái"
              class="flex-1"
            />
            <UButton @click="updateStatus" :loading="updatingStatus" :disabled="!updateItemId || !selectedStatus">
              <Icon name="lucide:save" class="w-4 h-4 mr-2" />
              PATCH Status
            </UButton>
          </div>
        </div>
      </div>

      <!-- Results Section -->
      <div class="space-y-4">
        <!-- Pending Items List -->
        <div v-if="pendingItems.length > 0" class="space-y-2">
          <h4 class="font-medium text-sm text-gray-700 dark:text-gray-300">Món cần quản lý:</h4>
          <div class="grid gap-2">
            <div 
              v-for="item in pendingItems" 
              :key="item.id"
              class="p-3 border rounded-lg space-y-2"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium">{{ item.menuItemName }}</span>
              </div>
              <div class="text-sm text-gray-600 dark:text-gray-400">
                <p>
                  Số lượng: {{ item.totalQuantity ?? (item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0) }}
                  | Đã hoàn thành: {{ item.completedQuantity || 0 }}
                  | Bàn: {{ item.tableName || 'N/A' }}
                </p>
                <p>Đơn hàng ID: {{ item.orderId }} | Giá: {{ formatPrice(item.totalPrice) }}</p>
                                 <p v-if="item.notes && item.notes.trim() !== ''" class="text-blue-600">
                   <Icon name="lucide:message-square" class="w-3 h-3 inline mr-1" />
                   Ghi chú: {{ item.notes }}
                 </p>
                 <p v-else class="text-xs text-gray-400 italic">
                   Không có ghi chú
                 </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Items by Order -->
        <div v-if="itemsByOrder.length > 0" class="space-y-2">
          <h4 class="font-medium text-sm text-gray-700 dark:text-gray-300">Món trong đơn hàng {{ orderId }}:</h4>
          <div class="grid gap-2">
            <div 
              v-for="item in itemsByOrder" 
              :key="item.id"
              class="p-3 border rounded-lg"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium">{{ item.menuItemName }}</span>
                <UBadge :color="getStatusColor(item.status)" variant="soft">
                  {{ item.statusDisplayName }}
                </UBadge>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                Số lượng: {{ item.totalQuantity ?? (item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0) }}
                | Đã hoàn thành: {{ item.completedQuantity || 0 }}
                | Giá: {{ formatPrice(item.totalPrice) }}
              </p>
                             <p v-if="item.notes && item.notes.trim() !== ''" class="text-sm text-blue-600">
                 <Icon name="lucide:message-square" class="w-3 h-3 inline mr-1" />
                 Ghi chú: {{ item.notes }}
               </p>
               <p v-else class="text-xs text-gray-400 italic">
                 Không có ghi chú
               </p>
            </div>
          </div>
        </div>

        <!-- Single Item -->
        <div v-if="singleItem" class="space-y-2">
          <h4 class="font-medium text-sm text-gray-700 dark:text-gray-300">Thông tin món {{ itemId }}:</h4>
          <div class="p-3 border rounded-lg">
            <div class="flex items-center justify-between mb-2">
              <span class="font-medium">{{ singleItem.menuItemName }}</span>
            </div>
                         <p v-if="singleItem.notes && singleItem.notes.trim() !== ''" class="text-sm text-blue-600 mb-2">
               <Icon name="lucide:message-square" class="w-3 h-3 inline mr-1" />
               Ghi chú: {{ singleItem.notes }}
             </p>
             <p v-else class="text-xs text-gray-400 italic mb-2">
               Không có ghi chú
             </p>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              Số lượng: {{ singleItem.totalQuantity ?? (singleItem.pendingQuantity || 0) + (singleItem.preparingQuantity || 0) + (singleItem.completedQuantity || 0) + (singleItem.servedQuantity || 0) }}
              | Đã hoàn thành: {{ singleItem.completedQuantity || 0 }}
              | Giá: {{ formatPrice(singleItem.totalPrice) }}
            </p>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="text-red-600 text-sm p-3 border border-red-200 rounded-lg bg-red-50">
          {{ error }}
        </div>

        <!-- Success Message -->
        <div v-if="successMessage" class="text-green-600 text-sm p-3 border border-green-200 rounded-lg bg-green-50">
          {{ successMessage }}
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { OrderItemApi, type OrderItemResponse } from '@/api-service'

// Reactive data
const pendingItems = ref<OrderItemResponse[]>([])
const itemsByOrder = ref<OrderItemResponse[]>([])
const singleItem = ref<OrderItemResponse | null>(null)
const orderId = ref('')
const itemId = ref('')
const updateItemId = ref('')
const selectedStatus = ref('')

// Loading states
const loadingPending = ref(false)
const loadingByOrder = ref(false)
const loadingById = ref(false)
const updatingStatus = ref(false)

// Messages
const error = ref('')
const successMessage = ref('')

// Status options for update
const statusOptions = [
  { label: 'Chờ xử lý', value: 'PENDING' },
  { label: 'Đang chế biến', value: 'PREPARING' },
  { label: 'Hoàn tất', value: 'COMPLETED' },
  { label: 'Đã phục vụ', value: 'SERVED' },
  { label: 'Đã hủy', value: 'CANCELLED' },
  { label: 'Hết món', value: 'OUT_OF_STOCK' }
]

// Methods
const loadPendingItems = async () => {
  try {
    loadingPending.value = true
    error.value = ''
    const response = await OrderItemApi.getPendingForManagement()
    pendingItems.value = response || []
    successMessage.value = `Tải ${pendingItems.value.length} món cần quản lý thành công!`
  } catch (err: any) {
    error.value = `Lỗi khi tải món cần quản lý: ${err.message || 'Unknown error'}`
    console.error('Error loading pending items:', err)
  } finally {
    loadingPending.value = false
  }
}

const loadItemsByOrderId = async () => {
  if (!orderId.value) return
  
  try {
    loadingByOrder.value = true
    error.value = ''
    const response = await OrderItemApi.getByOrderId(parseInt(orderId.value))
    itemsByOrder.value = response || []
    successMessage.value = `Tải ${itemsByOrder.value.length} món trong đơn hàng ${orderId.value} thành công!`
  } catch (err: any) {
    error.value = `Lỗi khi tải món theo đơn hàng: ${err.message || 'Unknown error'}`
    console.error('Error loading items by order:', err)
  } finally {
    loadingByOrder.value = false
  }
}

const loadItemById = async () => {
  if (!itemId.value) return
  
  try {
    loadingById.value = true
    error.value = ''
    const response = await OrderItemApi.getById(parseInt(itemId.value))
    singleItem.value = response || null
    successMessage.value = `Tải thông tin món ${itemId.value} thành công!`
  } catch (err: any) {
    error.value = `Lỗi khi tải thông tin món: ${err.message || 'Unknown error'}`
    console.error('Error loading item by id:', err)
  } finally {
    loadingById.value = false
  }
}

const updateStatus = async () => {
  if (!updateItemId.value || !selectedStatus.value) return
  
  try {
    updatingStatus.value = true
    error.value = ''
    
    const response = await OrderItemApi.updateStatus(parseInt(updateItemId.value), {
      status: selectedStatus.value
    })
    
    successMessage.value = `Cập nhật trạng thái món ${updateItemId.value} thành công!`
    
    // Refresh pending items if we're updating one
    if (pendingItems.value.some(item => item.id === parseInt(updateItemId.value))) {
      await loadPendingItems()
    }
    
    // Clear form
    updateItemId.value = ''
    selectedStatus.value = ''
    
  } catch (err: any) {
    error.value = `Lỗi khi cập nhật trạng thái: ${err.message || 'Unknown error'}`
    console.error('Error updating status:', err)
  } finally {
    updatingStatus.value = false
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'yellow'
    case 'PREPARING': return 'blue'
    case 'COMPLETED': return 'green'
    case 'SERVED': return 'purple'
    case 'CANCELLED': return 'red'
    case 'OUT_OF_STOCK': return 'gray'
    default: return 'gray'
  }
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Clear messages after 5 seconds
const clearMessages = () => {
  setTimeout(() => {
    error.value = ''
    successMessage.value = ''
  }, 5000)
}

// Watch for changes to clear messages
watch([error, successMessage], () => {
  if (error.value || successMessage.value) {
    clearMessages()
  }
})
</script>
