<template>
  <div class="space-y-4">
    <div class="relative flex items-center justify-center">
      <UButton size="lg" color="green" :loading="syncing" @click="addTable">
        <Icon name="lucide:plus" class="w-5 h-5 mr-2" />
        Thêm bàn
      </UButton>
      <UButton class="absolute right-0" size="sm" variant="outline" :loading="saving" @click="savePositions">
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
        <p v-if="!visibleTables.length" class="absolute inset-0 flex items-center justify-center text-gray-400">
          Chưa có bàn trong sơ đồ
        </p>
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
  DEFAULT_TABLE_AREA_ID,
  STANDARD_TABLE_NUMBERS,
  getDefaultTableName,
  getDefaultTablePosition,
  getStandardTableNumber,
  normalizeStandardTables,
} from "~/utils/tableLimits";

const tables = ref<any[]>([]);
const saving = ref(false);
const syncing = ref(false);

const dragging = ref<{ id: string; offsetX: number; offsetY: number } | null>(null);
const canvasRef = ref<HTMLElement | null>(null);

const visibleTables = computed(() =>
  normalizeStandardTables(tables.value).map((table: any) => ({
    ...table,
    tableNumber: getStandardTableNumber(table) || "",
    displayStatus: getTableDisplayStatus(table),
  })),
);

const load = async () => {
  tables.value = await TableApi.list();
};

const syncDefaultTables = async (showToast = false) => {
  syncing.value = true;
  try {
    const list = await TableApi.list();
    const standardTables = new Map<number, any>();

    (Array.isArray(list) ? list : [])
      .sort((a: any, b: any) => String(a.id).localeCompare(String(b.id)))
      .forEach((table: any) => {
        const tableNumber = getStandardTableNumber(table);
        if (tableNumber && !standardTables.has(tableNumber)) {
          standardTables.set(tableNumber, table);
        }
      });

    for (const tableNumber of STANDARD_TABLE_NUMBERS) {
      const position = getDefaultTablePosition(tableNumber);
      const body = {
        name: getDefaultTableName(tableNumber),
        capacity: 4,
        areaId: DEFAULT_TABLE_AREA_ID,
        posX: position.posX,
        posY: position.posY,
        tableStatus: "EMPTY",
      };
      const table = standardTables.get(tableNumber);

      if (table) {
        await TableApi.update(String(table.id), body);
      } else {
        await TableApi.create(body);
      }
    }

    await load();
    if (showToast) (useNuxtApp() as any).$toast?.success?.("Đã đồng bộ 20 bàn mặc định");
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e?.message || "Không thể đồng bộ bàn");
  } finally {
    syncing.value = false;
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
      areaId: DEFAULT_TABLE_AREA_ID,
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
      visibleTables.value.map((t) => ({ id: t.id, posX: t.posX || 0, posY: t.posY || 0 }))
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
  if (visibleTables.value.length < STANDARD_TABLE_NUMBERS.length) {
    await syncDefaultTables();
  }
  const nuxt = useNuxtApp() as any;
  nuxt?.$realtime?.subscribe?.("/topic/management/tables", (msg: any) => {
    if (msg?.type === "TABLE_STATUS_CHANGED") load();
  });
  nuxt?.$realtime?.subscribe?.("/topic/management/order-items", (msg: any) => {
    if (msg?.type === "ORDERED_UPDATED" || msg?.type === "ORDER_ITEM_STATUS_CHANGED") load();
  });
});
</script>
