<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
      <AppHeader title="Quản Lý" subtitle="Dashboard tổng quan" show-back-button>
        <template #actions>
          <UButton variant="outline" size="sm" @click="handleLogout">Đăng xuất</UButton>
        </template>
      </AppHeader>

      <div class="p-4">
        <UTabs v-model="activeTab" :items="tabs" class="mb-6">
          <template #item="{ item }">
            <div v-if="item.key === 'dashboard'" class="space-y-6">
              <AdminDashboardStats :stats="dashboardStats" />
              <AdminChartsSection
                :top-items="dashboardData?.topItems || []"
                :revenue-by-day="dashboardData?.revenueByDay || []"
              />
            </div>
            <div v-else-if="item.key === 'floor'">
              <AdminFloorPlanManager />
            </div>
            <div v-else-if="item.key === 'menu'">
              <AdminMenuManager
                :items="menuStore.items"
                :loading="loadingMenu"
                :error="menuError"
                :current-page="menuStore.page"
                :total-pages="menuStore.totalPages"
                :total-items="menuStore.totalItems || 0"
                :page-size="menuStore.size"
                @show-add-item="showAddItem = true"
                @edit-item="selectedItem = $event"
                @delete-item="deleteItem"
                @change-page="changePage"
                @change-page-size="changePageSize"
              />
            </div>
            <div v-else-if="item.key === 'staff'">
              <AdminStaffManager
                :staff="staffStore.staff"
                @show-add-staff="showAddStaff = true"
                @show-staff-detail="selectedStaff = $event"
                @edit-staff="selectedStaff = $event"
              />
            </div>
            <div v-else-if="item.key === 'reviews'">
              <AdminReviewsSection :reviews="mockReviews" />
            </div>
            <div v-else-if="item.key === 'order-status'">
              <OrderItemStatusManager />
            </div>
            <div v-else-if="item.key === 'qr'">
              <AdminQRManager />
            </div>
          </template>
        </UTabs>
      </div>

      <AdminAddItemModal
        v-model="showAddItem"
        :form="newItemForm"
        @close="showAddItem = false"
        @submit="addNewItem"
      />
      <AdminEditItemModal
        v-model="showEditItem"
        :item="selectedItem"
        @close="selectedItem = null"
        @save="updateItem"
      />
      <AdminStaffDetailModal
        v-model="showStaffDetail"
        :staff="selectedStaff"
        @close="selectedStaff = null"
        @edit="selectedStaff = $event"
      />
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useAdmin } from "~/composables/useAdmin";
import { useAuthStore } from "~/stores/auth";
import OrderItemStatusManager from "~/components/admin/OrderItemStatusManager.vue";
import AdminQRManager from "~/components/admin/QRManager.vue";

definePageMeta({
  middleware: "auth",
  roles: ["ADMIN", "MANAGER"],
});

useHead({ title: "Quản lý - Gọi Món" });

const auth = useAuthStore();
const {
  menuStore,
  staffStore,
  activeTab,
  selectedItem,
  selectedStaff,
  showAddItem,
  showAddStaff,
  showEditItem,
  showStaffDetail,
  newItemForm,
  tabs,
  mockReviews,
  dashboardStats,
  dashboardData,
  loadingMenu,
  menuError,
  addNewItem,
  updateItem,
  deleteItem,
  changePage,
  changePageSize,
} = useAdmin();

const handleLogout = () => {
  auth.logout();
  navigateTo("/login");
};
</script>
