<template>
  <div class="grid md:grid-cols-2 gap-6">
    <UCard>
      <template #header>
        <h3 class="text-lg font-semibold">Doanh thu theo ngày</h3>
      </template>
      <div class="h-64">
        <canvas ref="chartRef" />
      </div>
    </UCard>

    <UCard>
      <template #header>
        <h3 class="text-lg font-semibold">Món bán chạy nhất</h3>
      </template>
      <div class="space-y-3">
        <div
          v-for="(item, index) in topItems"
          :key="item.menuItemId || index"
          class="flex items-center justify-between"
        >
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center text-sm font-bold">
              {{ index + 1 }}
            </div>
            <div>
              <div class="font-medium">{{ item.name }}</div>
              <div class="text-sm text-gray-600">{{ item.quantity || 0 }} đơn</div>
            </div>
          </div>
          <div class="text-right font-semibold text-orange-600">
            {{ formatPrice(item.revenue || 0) }}
          </div>
        </div>
        <p v-if="!topItems?.length" class="text-gray-400 text-center py-8">Chưa có dữ liệu</p>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { Chart, registerables } from "chart.js";
import { formatPrice } from "~/utils/formatters";

Chart.register(...registerables);

interface TopItem {
  menuItemId?: number;
  name?: string;
  quantity?: number;
  revenue?: number;
}

interface RevenuePoint {
  date: string;
  revenue: number;
}

const props = defineProps<{
  topItems?: TopItem[];
  revenueByDay?: RevenuePoint[];
}>();

const chartRef = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart | null = null;

const renderChart = () => {
  if (!chartRef.value) return;
  chartInstance?.destroy();
  const points = props.revenueByDay || [];
  chartInstance = new Chart(chartRef.value, {
    type: "bar",
    data: {
      labels: points.map((p) => p.date),
      datasets: [
        {
          label: "Doanh thu (đ)",
          data: points.map((p) => Number(p.revenue)),
          backgroundColor: "rgba(249, 115, 22, 0.7)",
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
    },
  });
};

watch(() => props.revenueByDay, renderChart, { deep: true });
onMounted(renderChart);
onBeforeUnmount(() => chartInstance?.destroy());
</script>
