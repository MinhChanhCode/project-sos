<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-gray-900 text-white p-4">
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
      <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3 mt-4">
        <UCard v-for="item in pendingItems" :key="item.id" class="bg-gray-800">
          <div class="font-semibold">{{ item.menuItemName }}</div>
          <div class="text-sm text-gray-400">Bàn: {{ item.tableName || item.tableId }}</div>
          <div class="text-sm">SL: {{ item.pendingQuantity || item.totalQuantity }}</div>
          <UButton class="mt-2" size="sm" @click="startPreparing(item)">Bắt đầu chế biến</UButton>
          <UButton class="mt-2 ml-2" size="sm" color="green" @click="complete(item)">Hoàn thành</UButton>
        </UCard>
      </div>
      <p v-if="!pendingItems.length" class="text-center text-gray-500 mt-10">Không có món chờ</p>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { OrderItemApi } from "~/api-service/OrderItemApi";
import { useAuthStore } from "~/stores/auth";
import { useNotificationSound } from "~/utils/notificationSound";
import { navigateTo, useHead, useNuxtApp } from "nuxt/app";
import { onMounted, ref } from "vue";

definePageMeta({ middleware: "auth", roles: ["KITCHEN", "ADMIN"] });
useHead({ title: "Bếp - Gọi Món" });

const auth = useAuthStore();
const pendingItems = ref<any[]>([]);
const newOrderCount = ref(0);
const seenOrderIds = new Set<string>();
const { soundEnabled, toggleSound, playNotificationSound } = useNotificationSound();

const load = async () => {
  const items = await OrderItemApi.getPendingForManagement();
  pendingItems.value = Array.isArray(items) ? items : [];
  if (pendingItems.value.length === 0) newOrderCount.value = 0;
};

const startPreparing = async (item: any) => {
  await OrderItemApi.updateStatusQuick(item.id, "PREPARING");
  await load();
};

const complete = async (item: any) => {
  await OrderItemApi.updateStatusQuick(item.id, "COMPLETED");
  await load();
};

onMounted(async () => {
  await load();
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
    load();
  });
});
</script>
