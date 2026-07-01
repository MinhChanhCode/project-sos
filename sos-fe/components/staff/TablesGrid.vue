<template>
  <div>
    <div class="mb-4">
      <h2 class="text-lg font-semibold">Bàn được phân công</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400">Đồng bộ từ Admin &gt; Sơ đồ bàn, hiển thị {{ displayedTables.length }}/20 bàn.</p>
    </div>

    <div class="mb-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5">
      <button
        v-for="table in displayedTables"
        :key="`summary-${table.id}`"
        type="button"
        class="rounded-xl border bg-white p-3 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md dark:border-gray-800 dark:bg-gray-900"
        @click="$emit('select-table', table)"
      >
        <div class="mb-2 flex items-center justify-between gap-2">
          <div class="text-base font-bold text-gray-900 dark:text-white">Bàn {{ table.tableNumber }}</div>
          <span class="rounded-full px-2 py-1 text-[11px] font-bold ring-1" :class="getTableDisplayColorClass(table.displayStatus)">
            {{ table.displayStatus }}
          </span>
        </div>
        <div class="space-y-1 text-xs text-gray-600 dark:text-gray-300">
          <div class="flex justify-between"><span>Có khách</span><strong>{{ table.hasGuest ? 'Có' : 'Chưa' }}</strong></div>
          <div class="flex justify-between"><span>Order chờ</span><strong>{{ table.pendingCount > 0 ? 'Có' : 'Không' }}</strong></div>
          <div class="flex justify-between"><span>Cần phục vụ</span><strong>{{ table.serviceCount }} món</strong></div>
          <div class="flex justify-between"><span>Gần nhất</span><strong>{{ table.latestLabel }}</strong></div>
        </div>
      </button>
    </div>

    <div class="overflow-x-auto pb-2 mb-6">
      <div class="relative h-[900px] min-w-[1380px] overflow-hidden rounded-2xl border-2 border-dashed border-slate-500/80 bg-slate-900 shadow-inner">
        <div
          v-for="table in displayedTables"
          :key="table.id"
          class="absolute select-none"
          :style="{ left: `${table.posX || 0}px`, top: `${table.posY || 0}px` }"
        >
          <SharedFloorPlanTable
            :number="table.tableNumber"
            :status="table.displayStatus"
            readonly
            @select="$emit('select-table', table)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatOrderTime } from '~/utils/formatters'
import { getStandardTableNumber, normalizeStandardTables } from '~/utils/tableLimits'
import { getTableDisplayColorClass, getTableDisplayStatus, getTableOrderSummary } from '~/utils/tableStatus'

interface Table {
  id: string
  number: string
  status: "trống" | "đang đặt" | "chờ phục vụ" | "đang ăn" | "thanh toán" | "đã phục vụ" | "sẵn sàng" | "đang chế biến" | "có khách"
  customers: number
  assignedStaff: string
  totalAmount: number
  orders: Array<any>
  posX?: number
  posY?: number
  tableStatus?: string
  isAvailable?: boolean
  activeOrderId?: number | string | null
  tableNumber?: number | string | null
}

interface Props {
  tables: Table[]
}

const props = defineProps<Props>()

const displayedTables = computed(() =>
  normalizeStandardTables(props.tables).map((table) => {
    const summary = getTableOrderSummary(table)
    const displayStatus = getTableDisplayStatus(table)
    return {
      ...table,
      displayStatus,
      tableNumber: getStandardTableNumber(table) || "",
      hasGuest: displayStatus !== 'Trống',
      pendingCount: summary.pending + summary.preparing,
      serviceCount: summary.ready,
      latestLabel: summary.latestTime ? formatOrderTime(summary.latestTime) : 'Chưa có',
    }
  }),
)

defineEmits<{
  'select-table': [table: Table]
}>()
</script>
