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
          <button
            type="button"
            class="absolute -right-2 -top-2 flex h-8 w-8 items-center justify-center rounded-full border border-slate-700 bg-slate-950 text-slate-200 shadow-lg transition hover:border-emerald-400 hover:text-emerald-300"
            title="Chi tiết bàn"
            @mousedown.stop
            @click.stop="openTableDetail(table)"
          >
            <Icon name="lucide:settings-2" class="h-4 w-4" />
          </button>
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

    <UModal v-model="showDetail" :ui="{ width: 'max-w-2xl' }">
      <UCard v-if="selectedTable">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="text-lg font-semibold">Bàn {{ selectedTable.tableNumber || selectedTable.name }}</h3>
              <p class="text-sm text-slate-500">Chi tiết, trạng thái và thao tác nhanh</p>
            </div>
            <UBadge :color="selectedTable.displayStatus === 'Trống' ? 'gray' : 'green'" variant="soft">
              {{ selectedTable.displayStatus || selectedTable.tableStatus || selectedTable.status || "Trống" }}
            </UBadge>
          </div>
        </template>

        <div class="space-y-5">
          <div class="grid gap-3 md:grid-cols-2">
            <UFormGroup label="Tên bàn">
              <UInput v-model="tableForm.name" placeholder="Ví dụ: Bàn 1" />
            </UFormGroup>
            <UFormGroup label="Sức chứa">
              <UInput v-model.number="tableForm.capacity" type="number" min="1" />
            </UFormGroup>
            <UFormGroup label="Trạng thái">
              <select v-model="tableForm.tableStatus" class="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white">
                <option value="EMPTY">Trống</option>
                <option value="OCCUPIED">Có khách</option>
                <option value="ORDERING">Đang gọi món</option>
                <option value="WAITING_KITCHEN">Chờ bếp</option>
                <option value="READY">Món sẵn sàng</option>
                <option value="SERVING">Đang phục vụ</option>
                <option value="WAITING_PAYMENT">Chờ thanh toán</option>
                <option value="NEEDS_CLEANING">Cần dọn bàn</option>
              </select>
            </UFormGroup>
            <div class="rounded-lg border border-slate-800 p-3 text-sm">
              <div class="text-slate-400">Tổng tiền hiện tại</div>
              <div class="mt-1 text-xl font-bold">{{ formatMoney(Number(selectedTableDetail?.totalAmount || selectedTable.totalAmount || 0)) }}</div>
            </div>
          </div>

          <div class="rounded-lg border border-slate-800">
            <div class="flex items-center justify-between border-b border-slate-800 px-3 py-2">
              <span class="text-sm font-semibold">Món/order đang hoạt động</span>
              <UBadge color="blue" variant="soft">{{ selectedTableDetail?.sessionItems?.length || 0 }} món</UBadge>
            </div>
            <div class="max-h-56 overflow-y-auto divide-y divide-slate-800">
              <div
                v-for="item in selectedTableDetail?.sessionItems || []"
                :key="item.id"
                class="grid gap-2 px-3 py-2 text-sm md:grid-cols-[1fr_90px_90px_90px]"
              >
                <span class="font-medium">{{ item.menuItemName }}</span>
                <span>Chờ: <b>{{ item.pendingQuantity || 0 }}</b></span>
                <span>Sẵn: <b>{{ item.completedQuantity || 0 }}</b></span>
                <span>Phục vụ: <b>{{ item.servedQuantity || 0 }}</b></span>
              </div>
              <p v-if="!(selectedTableDetail?.sessionItems || []).length" class="px-3 py-6 text-center text-sm text-slate-500">
                Bàn này chưa có order đang xử lý
              </p>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex flex-wrap gap-2">
            <UButton color="green" :loading="savingDetail" @click="saveTableDetail">
              <Icon name="lucide:save" class="mr-1 h-4 w-4" />
              Lưu bàn
            </UButton>
            <UButton variant="outline" color="orange" :loading="clearingTable" @click="clearSelectedTable">
              <Icon name="lucide:broom" class="mr-1 h-4 w-4" />
              Dọn bàn
            </UButton>
            <UButton variant="outline" color="red" :loading="deletingTable" @click="deleteSelectedTable">
              <Icon name="lucide:trash-2" class="mr-1 h-4 w-4" />
              Xóa bàn
            </UButton>
            <UButton class="ml-auto" variant="ghost" @click="showDetail = false">Đóng</UButton>
          </div>
        </template>
      </UCard>
    </UModal>
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
const showDetail = ref(false);
const selectedTable = ref<any | null>(null);
const selectedTableDetail = ref<any | null>(null);
const savingDetail = ref(false);
const clearingTable = ref(false);
const deletingTable = ref(false);
const tableForm = ref({ name: "", capacity: 4, tableStatus: "EMPTY" });

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

const formatMoney = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value || 0);

const openTableDetail = async (table: any) => {
  selectedTable.value = table;
  tableForm.value = {
    name: table.name || getDefaultTableName(table.tableNumber),
    capacity: Number(table.capacity || 4),
    tableStatus: table.tableStatus || table.status || "EMPTY",
  };
  selectedTableDetail.value = null;
  showDetail.value = true;
  try {
    selectedTableDetail.value = await TableApi.getDetail(String(table.id));
  } catch {
    selectedTableDetail.value = null;
  }
};

const saveTableDetail = async () => {
  if (!selectedTable.value?.id) return;
  savingDetail.value = true;
  try {
    await TableApi.update(String(selectedTable.value.id), {
      name: tableForm.value.name,
      capacity: Number(tableForm.value.capacity || 1),
      tableStatus: tableForm.value.tableStatus,
      posX: Math.round(Number(selectedTable.value.posX || 0)),
      posY: Math.round(Number(selectedTable.value.posY || 0)),
    });
    await load();
    showDetail.value = false;
    (useNuxtApp() as any).$toast?.success?.("Đã lưu thông tin bàn");
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e?.message || "Không lưu được bàn");
  } finally {
    savingDetail.value = false;
  }
};

const clearSelectedTable = async () => {
  if (!selectedTable.value?.id) return;
  clearingTable.value = true;
  try {
    await TableApi.clear(String(selectedTable.value.id));
    await load();
    await openTableDetail(selectedTable.value);
    (useNuxtApp() as any).$toast?.success?.("Đã dọn bàn");
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e?.message || "Không dọn được bàn");
  } finally {
    clearingTable.value = false;
  }
};

const deleteSelectedTable = async () => {
  if (!selectedTable.value?.id) return;
  deletingTable.value = true;
  try {
    await TableApi.delete(String(selectedTable.value.id));
    showDetail.value = false;
    await load();
    (useNuxtApp() as any).$toast?.success?.("Đã xóa bàn");
  } catch (e: any) {
    (useNuxtApp() as any).$toast?.error?.(e?.message || "Không xóa được bàn đang có dữ liệu hoạt động");
  } finally {
    deletingTable.value = false;
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
