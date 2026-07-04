<template>
  <div class="space-y-4">
    <div class="relative flex items-center justify-center">
      <UButton size="lg" color="green" :loading="syncing || loading" @click="addTable">
        <Icon name="lucide:plus" class="w-5 h-5 mr-2" />
        Thêm bàn
      </UButton>
      <UButton class="absolute right-0" size="sm" variant="outline" :loading="saving" :disabled="!visibleTables.length" @click="savePositions">
        Lưu vị trí
      </UButton>
    </div>

    <div class="overflow-x-auto pb-2">
      <div
        ref="canvasRef"
        class="relative h-[900px] min-w-[1380px] overflow-hidden rounded-2xl border-2 border-dashed border-slate-500/80 bg-slate-900 shadow-inner"
        @mousemove="onDrag"
        @mouseup="endDrag"
        @mouseleave="endDrag"
      >
        <div
          v-for="table in visibleTables"
          :key="table.id"
          class="absolute cursor-move select-none"
          :style="{ left: `${table.posX || 0}px`, top: `${table.posY || 0}px` }"
          @mousedown="startDrag($event, table)"
        >
          <SharedFloorPlanTable :number="table.tableNumber" :status="table.displayStatus" />
        </div>
        <div v-if="loading" class="absolute inset-0 flex items-center justify-center text-sm font-semibold text-slate-300">
          Đang tải bàn từ database...
        </div>
        <div v-else-if="loadError" class="absolute inset-0 flex flex-col items-center justify-center gap-3 text-center text-sm text-rose-200">
          <p class="font-semibold">Không tải được sơ đồ bàn</p>
          <p class="max-w-md text-slate-300">{{ loadError }}</p>
          <UButton size="sm" variant="outline" color="green" @click="load">Tải lại</UButton>
        </div>
        <div v-else-if="!visibleTables.length" class="absolute inset-0 flex flex-col items-center justify-center gap-3 text-center text-sm text-slate-300">
          <p class="font-semibold">Database chưa có bàn nào</p>
          <p>Bấm “Thêm bàn” để tạo bàn thật và lưu vào database.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useNuxtApp } from "nuxt/app";
import { TableApi } from "~/api-service/TableApi";
import { getTableDisplayStatus } from "~/utils/tableStatus";
import {
  getDefaultTableName,
  getDefaultTablePosition,
  getStandardTableNumber,
  normalizeStandardTables,
} from "~/utils/tableLimits";

const tables = ref<any[]>([]);
const loading = ref(false);
const loadError = ref("");
const saving = ref(false);
const syncing = ref(false);

const dragging = ref<{ id: string; offsetX: number; offsetY: number } | null>(null);
const canvasRef = ref<HTMLElement | null>(null);

const visibleTables = computed(() => {
  const normalized = normalizeStandardTables(tables.value);
  const normalizedIds = new Set(normalized.map((table: any) => String(table.id)));
  const fallbackTables = tables.value
    .filter((table: any) => table?.id && !normalizedIds.has(String(table.id)))
    .sort((a: any, b: any) => String(a.name || a.id).localeCompare(String(b.name || b.id), "vi", { numeric: true }));

  return [...normalized, ...fallbackTables].map((table: any, index: number) => {
    const tableNumber = getStandardTableNumber(table) || index + 1;
    const position = getDefaultTablePosition(tableNumber);
    return {
      ...table,
      tableNumber,
      posX: table.posX || position.posX,
      posY: table.posY || position.posY,
      displayStatus: getTableDisplayStatus(table),
    };
  });
});

const load = async () => {
  loading.value = true;
  loadError.value = "";
  try {
    const list = await TableApi.list();
    tables.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    loadError.value = e?.message || "Không thể tải danh sách bàn từ backend";
    tables.value = [];
  } finally {
    loading.value = false;
  }
};

const addTable = async () => {
  syncing.value = true;
  try {
    const list = await TableApi.list();
    const usedNumbers = new Set(
      (Array.isArray(list) ? list : [])
        .map((table: any) => getStandardTableNumber(table))
        .filter(Boolean) as number[],
    );
    let tableNumber = 1;
    while (usedNumbers.has(tableNumber)) tableNumber += 1;
    const position = getDefaultTablePosition(tableNumber);
    await TableApi.create({
      name: getDefaultTableName(tableNumber),
      capacity: 4,
      posX: position.posX,
      posY: position.posY,
      tableStatus: "EMPTY",
    });
    await load();
    (useNuxtApp() as any).$toast?.success?.(`Đã thêm bàn ${tableNumber}`);
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e?.message || "Không thể thêm bàn");
  } finally {
    syncing.value = false;
  }
};

const startDrag = (e: MouseEvent, table: any) => {
  if (!canvasRef.value) return;
  const rect = canvasRef.value.getBoundingClientRect();
  dragging.value = {
    id: table.id,
    offsetX: e.clientX - rect.left - (table.posX || 0),
    offsetY: e.clientY - rect.top - (table.posY || 0),
  };
};

const onDrag = (e: MouseEvent) => {
  if (!dragging.value || !canvasRef.value) return;
  const rect = canvasRef.value.getBoundingClientRect();
  const table = tables.value.find((t) => t.id === dragging.value!.id);
  if (table) {
    table.posX = Math.max(0, Math.min(e.clientX - rect.left - dragging.value.offsetX, rect.width - 128));
    table.posY = Math.max(0, Math.min(e.clientY - rect.top - dragging.value.offsetY, rect.height - 128));
  }
};

const endDrag = () => {
  dragging.value = null;
};

const savePositions = async () => {
  saving.value = true;
  try {
    await TableApi.updatePositions(
      visibleTables.value
        .filter((t) => t.id)
        .map((t) => ({ id: t.id, posX: Math.round(Number(t.posX || 0)), posY: Math.round(Number(t.posY || 0)) }))
    );
    (useNuxtApp() as any).$toast?.success?.("Đã lưu vị trí bàn");
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e.message);
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  await load();
  const nuxt = useNuxtApp() as any;
  nuxt?.$realtime?.subscribe?.("/topic/management/tables", (msg: any) => {
    if (msg?.type === "TABLE_STATUS_CHANGED") load();
  });
  nuxt?.$realtime?.subscribe?.("/topic/management/order-items", (msg: any) => {
    if (msg?.type === "ORDERED_UPDATED" || msg?.type === "ORDER_ITEM_STATUS_CHANGED") load();
  });
});
</script>
