<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-xl font-semibold">Quản lý nhân viên</h2>
      <UButton @click="$emit('showAddStaff')">
        <Icon name="lucide:plus" class="w-4 h-4 mr-2" />
        Thêm nhân viên
      </UButton>
    </div>

    <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
      <UCard 
        v-for="member in staff" 
        :key="member.id"
        class="cursor-pointer hover:shadow-md transition-shadow"
      >
        <div class="p-4">
          <div class="flex items-center space-x-3 mb-3">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              <Icon name="lucide:users" class="w-6 h-6 text-blue-600" />
            </div>
            <div class="flex-1">
              <h3 class="font-semibold">{{ member.name }}</h3>
              <p class="text-sm text-gray-600">{{ member.role }}</p>
            </div>
            <UBadge :color="getStatusColor(member.status)" variant="soft">
              {{ getStatusText(member.status) }}
            </UBadge>
          </div>

          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span>Đánh giá:</span>
              <div class="flex items-center space-x-1">
                <Icon name="lucide:star" class="w-4 h-4 text-yellow-400" />
                <span>{{ member.rating }}</span>
              </div>
            </div>
            <div class="flex justify-between">
              <span>Đơn đã phục vụ:</span>
              <span class="font-semibold">{{ member.ordersServed }}</span>
            </div>
            <div v-if="member.tables.length > 0" class="flex justify-between">
              <span>Bàn phụ trách:</span>
              <span>{{ member.tables.join(', ') }}</span>
            </div>
          </div>

          <div class="flex space-x-2 mt-4">
            <UButton 
              @click="$emit('showStaffDetail', member)"
              variant="outline"
              class="flex-1"
              size="sm"
            >
              Chi tiết
            </UButton>
            <UButton 
              @click="$emit('editStaff', member)"
              variant="outline"
              size="sm"
              square
            >
              <Icon name="lucide:edit" class="w-4 h-4" />
            </UButton>
          </div>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StaffMember } from '~/stores/staff'
import { getStatusColor, getStatusText } from '~/utils/formatters'

interface Props {
  staff: StaffMember[]
}

defineProps<Props>()
defineEmits<{
  showAddStaff: []
  showStaffDetail: [member: StaffMember]
  editStaff: [member: StaffMember]
}>()
</script>