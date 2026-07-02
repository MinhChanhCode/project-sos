<template>
  <UModal v-model="isOpen" position="center">
    <UCard>
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-lg font-semibold">Giỏ hàng - Bàn {{ tableNumber }}</h2>
          <UButton
            variant="ghost"
            size="xs"
            square
            title="Đóng giỏ hàng"
            @click="emit('update:modelValue', false)"
          >
            <Icon name="lucide:x" class="h-4 w-4" />
          </UButton>
        </div>
      </template>

      <div v-if="isEmpty" class="text-center text-gray-500 py-8">
        <Icon name="lucide:shopping-cart" class="mx-auto mb-3 h-10 w-10 text-gray-300" />
        <p class="font-medium">Giỏ hàng đang trống</p>
        <p class="mt-1 text-sm">Hãy chọn món trước khi đặt.</p>
      </div>

      <div v-else class="space-y-3 max-h-96 overflow-y-auto">
        <div 
          v-for="item in items" 
          :key="item.id" 
          class="p-3 border rounded-lg space-y-2"
        >
          <div class="flex items-center space-x-3">
            <img
              :src="getCartImageUrl(item)"
              :alt="item.name" 
              class="w-12 h-12 object-cover rounded" 
              loading="lazy"
              @error="(event) => useCartFallbackImage(event, item)"
            />
            <div class="flex-1">
              <h4 class="font-medium text-sm">{{ item.name }}</h4>
              <p class="text-orange-600 font-semibold text-sm">{{ formatPrice(item.price) }}</p>
            </div>
            <div class="flex items-center space-x-2">
              <UButton 
                @click="$emit('updateQuantity', item.id, item.quantity - 1)"
                size="xs"
                variant="outline"
                square
              >
                <Icon name="lucide:minus" class="w-3 h-3" />
              </UButton>
              <UInput
                :model-value="item.quantity"
                @update:model-value="(value: string) => handleQuantityChange(item.id, value)"
                @keyup.enter="(event: KeyboardEvent) => handleQuantityChangeImmediate(item.id, (event.target as HTMLInputElement).value)"
                @blur="(event: FocusEvent) => handleQuantityChangeImmediate(item.id, (event.target as HTMLInputElement).value)"
                type="number"
                min="0"
                size="xs"
                class="w-12 text-center"
              />
              <UButton 
                @click="$emit('updateQuantity', item.id, item.quantity + 1)"
                size="xs"
                variant="outline"
                square
              >
                <Icon name="lucide:plus" class="w-3 h-3" />
              </UButton>
              <UButton
                color="red"
                variant="soft"
                size="xs"
                square
                title="Xóa món"
                @click="$emit('removeItem', item.id)"
              >
                <Icon name="lucide:trash-2" class="h-3.5 w-3.5" />
              </UButton>
            </div>
          </div>
          
          <!-- Ghi chú cho món -->
          <div class="ml-15">
            <UTextarea
              :model-value="item.note || ''"
              @update:model-value="(value: string) => $emit('updateNote', item.id, value)"
              placeholder="Ghi chú cho món này (không cay, thêm rau, ít muối...)"
              rows="2"
              class="text-xs"
              size="sm"
            />
          </div>
        </div>
      </div>

             <!-- Ordered items history -->
       <div v-if="orderedItems && orderedItems.length" class="mt-6">
         <div class="text-sm font-medium mb-2">Món đã đặt trước</div>
         <div class="space-y-3 max-h-56 overflow-y-auto">
           <div
             v-for="(item, idx) in groupedOrderedItems"
             :key="`ordered-${idx}-${item.id}`"
             class="flex items-center space-x-3 p-3 border rounded-lg"
           >
             <img
               :src="getCartImageUrl(item)"
               :alt="item.name"
               class="w-12 h-12 object-cover rounded"
               loading="lazy"
               @error="(event) => useCartFallbackImage(event, item)"
             />
             <div class="flex-1">
               <h4 class="font-medium text-sm">{{ item.name }}</h4>
               <p class="text-orange-600 font-semibold text-sm">{{ formatPrice(item.price) }}</p>
               <p class="text-xs text-gray-500">{{ formatOrderTime(item.orderTime) }}</p>
             </div>
             <div class="flex flex-col items-end space-y-1">
               <span class="text-gray-600 text-sm">x{{ item.quantity }}</span>
               <span class="text-xs px-2 py-1 rounded-full" :class="getStatusClass(item.status)">
                 {{ getStatusText(item.status) }}
               </span>
             </div>
           </div>
         </div>
         <!-- Total for ordered items -->
         <div class="mt-3 pt-3 border-t">
           <div class="flex justify-between items-center">
             <span class="text-sm font-medium">Tổng món đã đặt:</span>
             <span class="font-semibold text-orange-600">{{ formatPrice(orderedItemsTotal) }}</span>
           </div>
         </div>
       </div>

      <template #footer>
        <div class="flex justify-between items-center mb-4">
          <span class="font-semibold">Tổng cộng:</span>
          <span class="font-bold text-lg text-orange-600">{{ formatPrice(totalPrice) }}</span>
        </div>
        <div class="text-xs text-gray-500 mb-4 text-center">
          Bao gồm: {{ items.length }} món trong giỏ + {{ orderedItems?.length || 0 }} món đã đặt
        </div>
        <UButton class="w-full" :disabled="isEmpty" @click="$emit('confirmOrder')">
          Xác nhận đặt món
        </UButton>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount } from 'vue'
import type { CartItem } from '~/stores/cart'
import { formatPrice } from '~/utils/formatters'
import { getFoodFallbackImageUrl, getMenuImageUrl } from '~/utils/foodImages'

// Extend CartItem interface for ordered items
interface OrderedItem extends CartItem {
  orderTime?: string
  status?: string
}

interface Props {
  modelValue: boolean
  tableNumber: string
  items: CartItem[]
  orderedItems?: OrderedItem[]
  totalPrice: number
  isEmpty: boolean
  updatingQuantity?: Set<number>
}

const props = defineProps<Props>()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  updateQuantity: [id: number, quantity: number]
  updateNote: [id: number, note: string]
  removeItem: [id: number]
  confirmOrder: []
}>()

// Gộp các món giống nhau theo id, cộng dồn quantity
const groupedOrderedItems = computed(() => {
  const map = new Map<string, OrderedItem>()

  for (const item of props.orderedItems || []) {
    // Tạo key duy nhất cho mỗi món dựa trên id, note và status
    const key = `${item.id}__${item.note || ''}__${item.status || ''}`

    if (map.has(key)) {
      // Nếu đã có món giống, cộng dồn quantity
      const existingItem = map.get(key)!
      existingItem.quantity += item.quantity

      // Cập nhật thời gian order thành thời gian mới nhất
      if (item.orderTime && (!existingItem.orderTime || new Date(item.orderTime) > new Date(existingItem.orderTime))) {
        existingItem.orderTime = item.orderTime
      }
    } else {
      // Nếu chưa có, thêm mới
      map.set(key, { ...item })
    }
  }

  // Sắp xếp theo thời gian order (mới nhất trước)
  return Array.from(map.values()).sort((a, b) => {
    if (!a.orderTime && !b.orderTime) return 0
    if (!a.orderTime) return 1
    if (!b.orderTime) return -1
    return new Date(b.orderTime).getTime() - new Date(a.orderTime).getTime()
  })
})

// Computed total for ordered items
const orderedItemsTotal = computed(() => {
  return (groupedOrderedItems.value || []).reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
})

const getCartImageUrl = (item: CartItem) =>
  getMenuImageUrl(item.imageUrl, item.name, item.description, item.categoryId)

const useCartFallbackImage = (event: Event, item: CartItem) => {
  const image = event.target as HTMLImageElement
  const fallback = getFoodFallbackImageUrl(item.name, item.description, item.categoryId)
  if (image.src !== fallback) {
    image.src = fallback
  }
}

// Helper functions
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

const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': 'Chờ xử lý',
    'PREPARING': 'Đang làm',
    'COMPLETED': 'Hoàn thành',
    'SERVED': 'Đã phục vụ'
  }
  return statusMap[status || ''] || status || 'Chờ xử lý'
}

const getStatusClass = (status?: string) => {
  const classMap: Record<string, string> = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'PREPARING': 'bg-blue-100 text-blue-800',
    'COMPLETED': 'bg-green-100 text-green-800',
    'SERVED': 'bg-gray-100 text-gray-800'
  }
  return classMap[status || ''] || 'bg-yellow-100 text-yellow-800'
}

// Không còn sử dụng quantityTimers

const isUpdatingQuantity = (itemId: number) => {
  return props.updatingQuantity?.has(itemId) || false
}

const handleQuantityChange = (itemId: number, value: string) => {
  // Bỏ real-time khi đang nhập, chỉ lưu giá trị local
  // Không gọi emit updateQuantity
}

const handleQuantityChangeImmediate = (itemId: number, value: string) => {
  // Chỉ real-time khi blur hoặc enter
  const quantity = parseInt(value) || 0
  emit('updateQuantity', itemId, quantity)
}

// Không còn sử dụng timers
</script>
