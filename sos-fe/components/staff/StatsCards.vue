<template>
  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
    <UCard>
      <div class="text-center p-4">
        <div class="text-2xl font-bold text-blue-600">
          {{ stats.tablesAssigned }}
        </div>
        <div class="text-sm text-gray-600">Bàn phụ trách</div>
      </div>
    </UCard>
    <UCard>
      <div class="text-center p-4">
        <div class="text-2xl font-bold text-green-600">
          {{ stats.ordersServed }}
        </div>
        <div class="text-sm text-gray-600">Đơn đã phục vụ</div>
      </div>
    </UCard>
    <UCard>
      <div class="text-center p-4">
        <div class="text-2xl font-bold text-yellow-600">
          {{ stats.pendingOrders }}
        </div>
        <div class="text-sm text-gray-600">Đang chờ</div>
      </div>
    </UCard>
    <UCard>
      <div class="text-center p-4">
        <div class="text-2xl font-bold text-purple-600">
          {{ stats.averageRating }}
        </div>
        <div class="text-sm text-gray-600">Đánh giá TB</div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
interface StaffStats {
  tablesAssigned: number;
  ordersServed: number;
  pendingOrders: number;
  averageRating: number;
}

interface Props {
  stats: StaffStats;
}

defineProps<Props>();
</script>
