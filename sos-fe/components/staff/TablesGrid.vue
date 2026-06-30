<template>
  <div>
    <h2 class="text-lg font-semibold mb-4">Bàn được phân công</h2>
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
            :status="table.tableStatus"
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
import { getStandardTableNumber, normalizeStandardTables } from '~/utils/tableLimits'

interface Table {
  id: string
  number: string
  status: "trống" | "đang đặt" | "chờ phục vụ" | "đang ăn" | "thanh toán" | "đã phục vụ" | "sẵn sàng" | "đang chế biến"
  customers: number
  assignedStaff: string
  totalAmount: number
  orders: Array<any>
  posX?: number
  posY?: number
  tableStatus?: string
  tableNumber?: number | string | null
}

interface Props {
  tables: Table[]
}

const props = defineProps<Props>()

const displayedTables = computed(() =>
  normalizeStandardTables(props.tables).map((table) => ({
    ...table,
    tableNumber: getStandardTableNumber(table) || "",
  })),
)

defineEmits<{
  'select-table': [table: Table]
}>()
</script>
