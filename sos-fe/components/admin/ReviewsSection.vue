<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-xl font-semibold">Đánh giá & Phản hồi</h2>
    </div>

    <div class="space-y-4">
      <UCard v-for="review in reviews" :key="review.id">
        <div class="p-4">
          <div class="flex justify-between items-start mb-3">
            <div>
              <div class="font-medium">{{ review.customerName || 'Khách hàng' }}</div>
              <div class="text-sm text-gray-600">
                {{ review.tableName || (review.tableId ? `Bàn ${review.tableId}` : 'Chưa rõ bàn') }} · {{ formatDate(review.createdAt) }}
              </div>
              <div class="mt-1 text-xs text-gray-500">
                Session/Order: {{ review.sessionId || 'Chưa có' }}
              </div>
            </div>
            <div class="flex items-center space-x-1">
              <Icon
                v-for="i in 5"
                :key="i"
                name="lucide:star"
                :class="['w-4 h-4', i <= (review.rating || 0) ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300']"
              />
            </div>
          </div>
          <p class="text-gray-700 dark:text-gray-300 mb-2">{{ review.comment || 'Không có ghi chú' }}</p>
          <div class="flex flex-wrap gap-2">
            <UBadge v-if="review.sentiment" :color="sentimentColor(review.sentiment)" variant="soft" size="xs">
              AI: {{ review.sentiment }}
            </UBadge>
            <UBadge v-if="review.sentimentConfidence" color="gray" variant="soft" size="xs">
              Tin cậy: {{ Math.round(Number(review.sentimentConfidence) * 100) }}%
            </UBadge>
          </div>
        </div>
      </UCard>
      <p v-if="!reviews?.length" class="text-center text-gray-400 py-8">Chưa có đánh giá</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Review {
  id: number
  tableId?: string
  tableName?: string
  customerName?: string
  rating?: number
  comment?: string
  sentiment?: string
  sentimentConfidence?: number | string
  sessionId?: string
  createdAt?: string
}

defineProps<{ reviews: Review[] }>();

const formatDate = (d?: string) => (d ? new Date(d).toLocaleString("vi-VN") : "");
const sentimentColor = (s: string) => {
  if (s === "POSITIVE") return "green";
  if (s === "NEGATIVE") return "red";
  return "gray";
};
</script>
