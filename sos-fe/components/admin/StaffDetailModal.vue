<template>
  <UModal v-model="isOpen">
    <UCard v-if="staff">
      <template #header>
        <h2 class="text-lg font-semibold">Chi tiết nhân viên</h2>
      </template>

      <div class="space-y-4">
        <div class="flex items-center space-x-4">
          <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
            <Icon name="lucide:users" class="w-8 h-8 text-blue-600" />
          </div>
          <div>
            <h3 class="text-lg font-semibold">{{ staff.name }}</h3>
            <p class="text-gray-600">{{ staff.role }}</p>
            <UBadge :color="getStatusColor(staff.status)" variant="soft">
              {{ getStatusText(staff.status) }}
            </UBadge>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="text-center p-4 bg-gray-50 rounded-lg">
            <div class="text-2xl font-bold text-yellow-600">{{ staff.rating }}</div>
            <div class="text-sm text-gray-600">Đánh giá trung bình</div>
          </div>
          <div class="text-center p-4 bg-gray-50 rounded-lg">
            <div class="text-2xl font-bold text-green-600">{{ staff.ordersServed }}</div>
            <div class="text-sm text-gray-600">Đơn đã phục vụ</div>
          </div>
        </div>

        <div v-if="staff.tables.length > 0">
          <h4 class="font-medium mb-2">Bàn phụ trách:</h4>
          <div class="flex flex-wrap gap-2">
            <UBadge 
              v-for="table in staff.tables" 
              :key="table" 
              variant="outline"
              size="xs"
            >
              {{ table }}
            </UBadge>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex space-x-2">
          <UButton @click="$emit('close')" variant="outline" class="flex-1">
            Đóng
          </UButton>
          <UButton @click="$emit('edit', staff)" class="flex-1">
            Chỉnh sửa
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StaffMember } from '~/stores/staff'
import { getStatusColor, getStatusText } from '~/utils/formatters'

interface Props {
  modelValue: boolean
  staff: StaffMember | null
}

const props = defineProps<Props>()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
  edit: [staff: StaffMember]
}>()
</script>