<template>
  <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
    <UCard>
      <div class="p-4 flex items-center space-x-2">
        <Icon name="lucide:bar-chart-3" class="w-8 h-8 text-blue-600" />
        <div>
          <div class="text-2xl font-bold">{{ stats.activeTables }}</div>
          <div class="text-sm text-gray-600">Bàn hoạt động</div>
        </div>
      </div>
    </UCard>
    <UCard>
      <div class="p-4 flex items-center space-x-2">
        <Icon name="lucide:dollar-sign" class="w-8 h-8 text-green-600" />
        <div>
          <div class="text-2xl font-bold">{{ formatPrice(stats.todayRevenue) }}</div>
          <div class="text-sm text-gray-600">Doanh thu hôm nay</div>
        </div>
      </div>
    </UCard>
    <UCard>
      <div class="p-4 flex items-center space-x-2">
        <Icon name="lucide:trending-up" class="w-8 h-8 text-orange-600" />
        <div>
          <div class="text-2xl font-bold">{{ stats.totalOrders }}</div>
          <div class="text-sm text-gray-600">Đơn hàng</div>
        </div>
      </div>
    </UCard>
    <UCard>
      <div class="p-4 flex items-center space-x-2">
        <Icon name="lucide:star" class="w-8 h-8 text-yellow-600" />
        <div>
          <div class="text-2xl font-bold">{{ stats.averageRating }}</div>
          <div class="text-sm text-gray-600">Đánh giá TB</div>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { formatPrice } from '~/utils/formatters'

interface Stats {
  activeTables: number
  todayRevenue: number
  totalOrders: number
  averageRating: number
}

interface Props {
  stats: Stats
}

defineProps<Props>()
</script>