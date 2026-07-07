<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-slate-950 text-white p-4">
      <AppHeader title="Bếp" subtitle="Kitchen Display System" show-back-button>
        <template #actions>
          <UBadge v-if="newOrderCount > 0" color="red" variant="solid">{{ newOrderCount }} mới</UBadge>
          <UButton variant="ghost" size="sm" @click="toggleSound">
            <Icon :name="soundEnabled ? 'lucide:volume-2' : 'lucide:volume-x'" class="mr-1 h-4 w-4" />
            Chuông
          </UButton>
          <UButton variant="outline" size="sm" @click="auth.logout(); navigateTo('/login')">Đăng xuất</UButton>
        </template>
      </AppHeader>

      <section class="mt-4 grid gap-3 md:grid-cols-5">
        <div v-for="stat in kitchenStats" :key="stat.label" class="rounded-lg border border-slate-800 bg-slate-900 p-3">
          <div class="text-xs text-slate-400">{{ stat.label }}</div>
          <div class="mt-1 text-2xl font-bold">{{ stat.value }}</div>
        </div>
      </section>

      <section class="mt-4 flex flex-col gap-3 rounded-lg border border-slate-800 bg-slate-900 p-3 lg:flex-row lg:items-end">
        <div class="w-full lg:w-64">
          <ULabel class="mb-1 text-slate-300">Lọc bàn</ULabel>
          <select v-model="selectedTable" class="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm">
            <option v-for="option in tableOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
        <div class="w-full lg:w-64">
          <ULabel class="mb-1 text-slate-300">Lọc danh mục</ULabel>
          <select v-model="selectedCategory" class="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm">
            <option v-for="option in categoryOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
        <UCheckbox v-model="showLateOnly" label="Chỉ món đang trễ" />
        <UButton variant="soft" color="gray" @click="loadAll">
          <Icon name="lucide:refresh-cw" class="mr-1 h-4 w-4" />
          Làm mới
        </UButton>
      </section>

      <section class="mt-4 grid gap-4 xl:grid-cols-4">
        <div
          v-for="column in columns"
          :key="column.key"
          class="min-h-[360px] rounded-lg border border-slate-800 bg-slate-900"
        >
          <div class="flex items-center justify-between border-b border-slate-800 px-4 py-3">
            <div>
              <h2 class="font-semibold">{{ column.title }}</h2>
              <p class="text-xs text-slate-400">{{ column.description }}</p>
            </div>
            <UBadge :color="column.color" variant="soft">{{ column.items.length }}</UBadge>
          </div>

          <div class="space-y-3 p-3">
            <article
              v-for="item in column.items"
              :key="`${column.key}-${item.id}`"
              class="rounded-lg border border-slate-800 bg-slate-950 p-3"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <div class="flex flex-wrap items-center gap-2">
                    <div class="font-semibold">{{ item.menuItemName }}</div>
                    <UBadge :color="isDrinkItem(item) ? 'blue' : 'orange'" variant="soft" size="xs">
                      {{ isDrinkItem(item) ? "Đồ uống" : "Bếp" }}
                    </UBadge>
                  </div>
                  <div class="mt-1 text-sm text-slate-400">Bàn: {{ item.tableName || item.tableId || "Chưa rõ" }}</div>
                </div>
                <UBadge v-if="isLate(item)" color="red" variant="solid">Trễ</UBadge>
              </div>
              <div class="mt-3 grid grid-cols-2 gap-2 text-sm text-slate-300">
                <div>Chờ: <b>{{ item.pendingQuantity || 0 }}</b></div>
                <div>Đang làm: <b>{{ item.preparingQuantity || 0 }}</b></div>
                <div>Sẵn sàng: <b>{{ item.completedQuantity || 0 }}</b></div>
                <div>SL tính tiền: <b>{{ item.totalQuantity || 0 }}</b></div>
              </div>
              <p v-if="item.notes" class="mt-3 rounded-md bg-amber-500/10 px-3 py-2 text-sm text-amber-100">
                Ghi chú: {{ item.notes }}
              </p>
              <div class="mt-3 flex flex-wrap gap-2">
                <UButton
                  v-if="(item.pendingQuantity || 0) > 0"
                  size="xs"
                  color="yellow"
                  @click="startPreparing(item)"
                >
                  Bắt đầu
                </UButton>
                <UButton
                  v-if="(item.preparingQuantity || 0) > 0"
                  size="xs"
                  color="green"
                  @click="markReady(item)"
                >
                  Sẵn sàng
                </UButton>
              </div>
            </article>
            <p v-if="!column.items.length" class="py-8 text-center text-sm text-slate-500">Không có món</p>
          </div>
        </div>
      </section>

      <section class="mt-4 rounded-lg border border-slate-800 bg-slate-900">
        <div class="flex items-center justify-between border-b border-slate-800 px-4 py-3">
          <div>
            <h2 class="font-semibold">Lịch sử món đã làm</h2>
            <p class="text-xs text-slate-400">Các món đã sẵn sàng, đã phục vụ, hủy hoặc báo hết trong ca hiện tại</p>
          </div>
          <UBadge color="blue" variant="soft">{{ historyItems.length }} mục</UBadge>
        </div>
        <div class="max-h-80 overflow-y-auto divide-y divide-slate-800">
          <article
            v-for="item in historyItems"
            :key="`history-${item.id}`"
            class="grid gap-2 px-4 py-3 text-sm md:grid-cols-[1fr_160px_160px_120px]"
          >
            <div>
              <div class="flex flex-wrap items-center gap-2 font-medium">
                <span>{{ item.menuItemName }}</span>
                <UBadge :color="isDrinkItem(item) ? 'blue' : 'orange'" variant="soft" size="xs">
                  {{ isDrinkItem(item) ? "Đồ uống" : "Bếp" }}
                </UBadge>
              </div>
              <p class="mt-1 text-xs text-slate-400">Bàn: {{ item.tableName || item.tableId || "Chưa rõ" }}</p>
            </div>
            <div class="text-slate-300">Sẵn sàng: <b>{{ item.completedQuantity || 0 }}</b></div>
            <div class="text-slate-300">Đã phục vụ: <b>{{ item.servedQuantity || 0 }}</b></div>
            <div>
              <UBadge :color="getHistoryColor(item)" variant="soft">{{ getHistoryLabel(item) }}</UBadge>
            </div>
          </article>
          <p v-if="!historyItems.length" class="px-4 py-8 text-center text-sm text-slate-500">
            Chưa có món trong lịch sử ca này
          </p>
        </div>
      </section>

      <section class="mt-4 rounded-lg border border-slate-800 bg-slate-900">
        <div class="flex items-center justify-between border-b border-slate-800 px-4 py-3">
          <div>
            <h2 class="font-semibold">Còn món / hết món</h2>
            <p class="text-xs text-slate-400">Bếp đổi trạng thái, Customer cập nhật realtime</p>
          </div>
          <UBadge color="orange" variant="soft">{{ unavailableCount }} hết món</UBadge>
        </div>
        <div class="grid gap-3 p-3 md:grid-cols-2 xl:grid-cols-4">
          <article v-for="item in menuItems" :key="item.id" class="rounded-lg border border-slate-800 bg-slate-950 p-3">
            <div class="font-medium">{{ item.name }}</div>
            <div class="mt-1 text-xs text-slate-400">{{ item.categoryName || item.categoryId }}</div>
            <div class="mt-3 flex items-center justify-between">
              <UBadge :color="item.isAvailable === false ? 'red' : 'green'" variant="soft">
                {{ item.isAvailable === false ? "Hết món" : "Còn món" }}
              </UBadge>
              <UButton size="xs" variant="outline" @click="toggleAvailability(item)">
                {{ item.isAvailable === false ? "Bật lại" : "Báo hết" }}
              </UButton>
            </div>
          </article>
        </div>
      </section>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { OrderItemApi } from "~/api-service/OrderItemApi";
import { menuApi } from "~/api-service/MenuApi";
import { categoryApi } from "~/api-service/ExtendedApi";
import { useAuthStore } from "~/stores/auth";
import { useNotificationSound } from "~/utils/notificationSound";
import { navigateTo, useHead, useNuxtApp } from "nuxt/app";
import { computed, onMounted, ref } from "vue";

definePageMeta({ middleware: "auth", roles: ["KITCHEN", "ADMIN"] });
useHead({ title: "Bếp - Gọi Món" });

const auth = useAuthStore();
const orderItems = ref<any[]>([]);
const menuItems = ref<any[]>([]);
const categories = ref<any[]>([]);
const selectedTable = ref("all");
const selectedCategory = ref("all");
const showLateOnly = ref(false);
const newOrderCount = ref(0);
const seenOrderIds = new Set<string>();
const { soundEnabled, toggleSound, playNotificationSound } = useNotificationSound();

const loadOrders = async () => {
  const items = await OrderItemApi.getPendingForManagement();
  orderItems.value = Array.isArray(items) ? items : [];
  if (orderItems.value.length === 0) newOrderCount.value = 0;
};

const loadMenu = async () => {
  const [menuPage, categoryList] = await Promise.all([
    menuApi.getActivePaged(0, 200),
    categoryApi.list().catch(() => []),
  ]);
  const rawItems = Array.isArray((menuPage as any)?.data?.content)
    ? (menuPage as any).data.content
    : (menuPage as any)?.content || [];
  menuItems.value = rawItems;
  categories.value = Array.isArray(categoryList) ? categoryList : [];
};

const loadAll = async () => {
  await Promise.all([loadOrders(), loadMenu()]);
};

const categoryById = computed(() => {
  const map = new Map<string, any>();
  categories.value.forEach((category: any) => map.set(String(category.id), category));
  return map;
});

const tableOptions = computed(() => [
  { label: "Tất cả bàn", value: "all" },
  ...Array.from(new Set(orderItems.value.map((item: any) => item.tableName).filter(Boolean)))
    .sort()
    .map((tableName) => ({ label: String(tableName), value: String(tableName) })),
]);

const categoryOptions = computed(() => [
  { label: "Tất cả danh mục", value: "all" },
  ...categories.value.map((category: any) => ({ label: String(category.name), value: String(category.id) })),
]);

const filteredItems = computed(() => {
  return orderItems.value.filter((item: any) => {
    if (selectedTable.value !== "all" && String(item.tableName) !== selectedTable.value) return false;
    if (selectedCategory.value !== "all") {
      const menuItem = menuItems.value.find((menu: any) => Number(menu.id) === Number(item.menuItemId));
      if (String(menuItem?.categoryId || "") !== selectedCategory.value) return false;
    }
    if (showLateOnly.value && !isLate(item)) return false;
    return true;
  });
});

const isLate = (item: any) => {
  if (!item.orderTime) return false;
  const started = new Date(item.orderTime).getTime();
  if (!Number.isFinite(started)) return false;
  const menuItem = menuItems.value.find((menu: any) => Number(menu.id) === Number(item.menuItemId));
  const minutes = Number(menuItem?.prepTimeMinutes || 20);
  return Date.now() - started > minutes * 60 * 1000;
};

const getMenuItem = (item: any) =>
  menuItems.value.find((menu: any) => Number(menu.id) === Number(item.menuItemId));

const isDrinkItem = (item: any) => {
  const menuItem = getMenuItem(item);
  const category = categoryById.value.get(String(menuItem?.categoryId || ""));
  const text = `${menuItem?.type || ""} ${category?.name || menuItem?.categoryName || ""} ${item.menuItemName || ""}`.toLowerCase();
  return ["drink", "đồ uống", "do uong", "bia", "nước", "nuoc", "trà", "tra", "cafe", "cà phê"].some((keyword) =>
    text.includes(keyword),
  );
};

const getColumnItems = (status: "pending" | "preparing" | "ready" | "done") => {
  return filteredItems.value.filter((item: any) => {
    if (status === "pending") return (item.pendingQuantity || 0) > 0;
    if (status === "preparing") return (item.preparingQuantity || 0) > 0;
    if (status === "ready") return (item.completedQuantity || 0) > 0;
    return (item.servedQuantity || 0) > 0;
  });
};

const historyItems = computed(() =>
  filteredItems.value
    .filter((item: any) =>
      (item.completedQuantity || 0) > 0 ||
      (item.servedQuantity || 0) > 0 ||
      (item.cancelledQuantity || 0) > 0 ||
      (item.outOfStockQuantity || 0) > 0,
    )
    .slice()
    .sort((a: any, b: any) => new Date(b.orderTime || 0).getTime() - new Date(a.orderTime || 0).getTime()),
);

const getHistoryLabel = (item: any) => {
  if ((item.outOfStockQuantity || 0) > 0) return "Hết món";
  if ((item.cancelledQuantity || 0) > 0) return "Đã hủy";
  if ((item.servedQuantity || 0) > 0) return "Đã phục vụ";
  return "Sẵn sàng";
};

const getHistoryColor = (item: any) => {
  if ((item.outOfStockQuantity || 0) > 0) return "red";
  if ((item.cancelledQuantity || 0) > 0) return "gray";
  if ((item.servedQuantity || 0) > 0) return "blue";
  return "green";
};

const columns = computed(() => [
  { key: "pending", title: "Mới nhận", description: "Món đang chờ bếp", color: "gray", items: getColumnItems("pending") },
  { key: "preparing", title: "Đang chế biến", description: "Món bếp đang làm", color: "yellow", items: getColumnItems("preparing") },
  { key: "ready", title: "Sẵn sàng phục vụ", description: "Báo nhân viên mang ra", color: "green", items: getColumnItems("ready") },
  { key: "done", title: "Đã hoàn thành", description: "Đã phục vụ khách", color: "blue", items: getColumnItems("done") },
]);

const kitchenStats = computed(() => [
  { label: "Order mới", value: newOrderCount.value },
  { label: "Món chờ", value: getColumnItems("pending").length },
  { label: "Đang chế biến", value: getColumnItems("preparing").length },
  { label: "Sẵn sàng", value: getColumnItems("ready").length },
  { label: "Món hết", value: unavailableCount.value },
]);

const unavailableCount = computed(() => menuItems.value.filter((item: any) => item.isAvailable === false).length);

const startPreparing = async (item: any) => {
  await OrderItemApi.updateStatusQuick(item.id, "PREPARING");
  await loadOrders();
};

const markReady = async (item: any) => {
  await OrderItemApi.updateStatusQuick(item.id, "COMPLETED");
  await loadOrders();
};

const toggleAvailability = async (item: any) => {
  await menuApi.toggleAvailability(item.id);
  await loadMenu();
};

onMounted(async () => {
  await loadAll();
  const nuxt = useNuxtApp() as any;
  nuxt.$realtime?.onKitchenOrders?.((msg: any) => {
    if (msg?.type === "ORDER_CREATED") {
      const key = String(msg.orderId || Date.now());
      if (!seenOrderIds.has(key)) {
        seenOrderIds.add(key);
        newOrderCount.value += 1;
        playNotificationSound();
      }
    }
    loadOrders();
  });
  nuxt.$realtime?.subscribe?.("/topic/menu-items", (msg: any) => {
    if (msg?.type !== "MENU_ITEM_CHANGED" || !msg.item?.id) return;
    const index = menuItems.value.findIndex((item: any) => Number(item.id) === Number(msg.item.id));
    if (index >= 0) menuItems.value[index] = msg.item;
    else menuItems.value.unshift(msg.item);
  });
});
</script>
