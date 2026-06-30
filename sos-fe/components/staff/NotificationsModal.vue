<template>
  <UModal v-model="isOpen" @close="handleClose">
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Thông báo</h2>
      </template>

      <div class="space-y-3 max-h-96 overflow-y-auto">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="flex items-start space-x-3 p-3 border rounded-lg"
        >
          <div class="flex-shrink-0 mt-1">
            <Icon 
              :name="getNotificationIcon(notification.type)" 
              :class="getNotificationIconColor(notification.type)"
              class="w-4 h-4"
            />
          </div>
          <div class="flex-1">
            <p class="text-sm">{{ notification.message }}</p>
            <p class="text-xs text-gray-500">{{ notification.time }}</p>
          </div>
        </div>
      </div>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStaffStore } from '@/stores/staff'

interface Notification {
  id: string
  type: 'kitchen' | 'call' | 'order'
  message: string
  time: string
}

interface Props {
  modelValue: boolean
  notifications: Notification[]
}

const props = defineProps<Props>()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const getNotificationIcon = (type: string) => {
  const icons: Record<string, string> = {
    'kitchen': 'lucide:chef-hat',
    'call': 'lucide:bell',
    'order': 'lucide:utensils'
  }
  return icons[type] || 'lucide:bell'
}

const getNotificationIconColor = (type: string) => {
  const colors: Record<string, string> = {
    'kitchen': 'text-orange-500',
    'call': 'text-blue-500',
    'order': 'text-green-500'
  }
  return colors[type] || 'text-gray-500'
}

const handleClose = () => {
  const store = useStaffStore()
  store.markAllRead()
}
</script>