<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
      <!-- Header -->
      <AppHeader 
        title="Nhân Viên" 
        :subtitle="`${staffRole === 'server' ? 'Phục vụ' : 'Bếp'} - Ca sáng`"
        show-back-button
      >
        <template #actions>
          <UButton variant="ghost" size="sm" @click="toggleSound">
            <Icon :name="soundEnabled ? 'lucide:volume-2' : 'lucide:volume-x'" class="mr-1 h-4 w-4" />
            Chuông
          </UButton>
          <UButton variant="outline" size="sm" @click="auth.logout(); navigateTo('/login')">Đăng xuất</UButton>
          <UButton 
            @click="showNotifications = true" 
            variant="outline" 
            size="sm"
          >
            <Icon name="lucide:bell" class="w-4 h-4" />
            <UBadge v-if="staffStore.pendingNotifications > 0" color="red" variant="solid" class="ml-2">
              {{ staffStore.pendingNotifications }}
            </UBadge>
          </UButton>
        </template>
      </AppHeader>

      <!-- Stats -->
      <div class="p-4">
        <StaffStatsCards :stats="staffStats" />
      </div>

      <!-- Tables Grid -->
      <div class="px-4">
        <StaffTablesGrid 
          :tables="staffStore.tables"
          @select-table="handleTableSelect"
        />
      </div>

      <!-- Service Request Manager -->
      <div class="px-4 pb-6">
        <StaffServiceRequestManager />
      </div>

      <div data-staff-order-status>
        <StaffOrderItemStatusManager
          :table-id="selectedTable?.id"
          :order-id="selectedTable?.orders?.[0]?.id"
          :table-number="selectedTable?.number"
          :focus-type="staffOrderFocus"
        />
      </div>

      <!-- Quick Actions -->
      <div class="px-4 pb-6">
        <StaffQuickActions @action="handleQuickAction" />
      </div>

      <!-- Modals -->
      <StaffTableDetailModal
        v-model="showTableDetail"
        :table="selectedTable"
        @update-item-status="handleUpdateItemStatus"
        @chat-customer="handleChatCustomer"
        @complete-table="handleCompleteTable"
      />

      <StaffNotificationsModal
        v-model="showNotifications"
        :notifications="staffStore.notifications"
      />

      <StaffChatInbox
        v-model="showStaffChat"
        :tables="staffStore.tables"
        :initial-table-id="activeChatTableId"
      />
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useStaff } from '~/composables/useStaff'
import { useAuthStore } from '~/stores/auth'
import { navigateTo, useHead } from 'nuxt/app'
import { onMounted, watch } from 'vue'
import StaffOrderItemStatusManager from '@/components/staff/OrderItemStatusManager.vue'

// Meta
definePageMeta({
  middleware: "auth",
  roles: ["STAFF", "ADMIN", "MANAGER"],
});

useHead({
  title: 'Nhân viên - Gọi Món'
})

// Use composable
const auth = useAuthStore()
const {
  staffStore,
  selectedTable,
  showNotifications,
  showStaffChat,
  activeChatTableId,
  staffOrderFocus,
  staffRole,
  showTableDetail,
  staffStats,
  handleQuickAction,
  handleTableSelect,
  handleUpdateItemStatus,
  handleChatCustomer,
  handleCompleteTable,
  ensureTablesLoaded,
  setupRealtimeSubscriptions,
  soundEnabled,
  toggleSound
} = useStaff()

onMounted(() => { 
  ensureTablesLoaded(); 
  setupRealtimeSubscriptions();
})

watch(
  () => staffStore.tables,
  (newTables) => {
    const current = selectedTable.value
    if (current) {
      const updated = newTables.find(t => t.id === current.id)
      if (updated) {
        selectedTable.value = updated
      }
    }
  },
  { deep: true }
)
</script>
